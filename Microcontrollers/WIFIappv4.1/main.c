//*****************************************************************************
// Authors: Salman Mian & M. Yahia Al Kahf

// Program description:
//		This program enables:
//			1. Wi-Fi connectivity via TI's SmartConfig and
//			   WPS.
//			2. Handles received Wi-Fi messages from Hub
//			3. Generation of signal driving the uktrasound transmitter
//			4. The capture of ultrasound via ADC sampling
//****************************************************************************

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <stdbool.h>
#include <stdint.h>

// Simplelink includes
#include "simplelink.h"
#include "wlan.h"

//Driverlib includes
#include "hw_types.h"
#include "hw_common_reg.h"
#include "hw_ints.h"
#include "hw_adc.h"
#include "hw_memmap.h"
#include "rom.h"
#include "rom_map.h"
#include "interrupt.h"
#include "prcm.h"
#include "utils.h"
#include "uart.h"

#include "timer_if.h"
#include "timer.h"
#include "gpio.h"
#include "pin.h"
#include "adc.h"
#include "systick.h"

//Common interface includes
#include "pinmux.h"
#include "gpio_if.h"
#include "common.h"
#include "adc_userinput.h"

#include "button_if.h"

#ifndef NOTERM
#include "uart_if.h"
#endif

#define DUTYCYCLE_GRANULARITY   4 //157

#define STATE                   "Slave " // Device can either be Master, Slave or Band
//Add space for sending delimiter to HUB

#define IP_ADDR            0xFFFFFFFF //255.255.255.255
#define PORT_NUM           8050
#define BUF_SIZE           1400

#define WLAN_DEL_ALL_PROFILES   0xFF

// Application specific status/error codes
typedef enum{
    // Choosing -0x7D0 to avoid overlap w/ host-driver's error codes
    LAN_CONNECTION_FAILED = -0x7D0,
    DEVICE_NOT_IN_STATION_MODE = LAN_CONNECTION_FAILED - 1,

	SOCKET_CREATE_ERROR = -0x7D0,
	BIND_ERROR = SOCKET_CREATE_ERROR - 1,
	SEND_ERROR = BIND_ERROR - 1,
	RECV_ERROR = SEND_ERROR -1,
	SOCKET_CLOSE = RECV_ERROR -1,

	STATUS_CODE_MAX = -0xBB8
}e_AppStatusCodes;


//*****************************************************************************
//                 GLOBAL VARIABLES -- Start
//*****************************************************************************
volatile unsigned long  g_ulStatus = 0;//SimpleLink Status
unsigned long  g_ulGatewayIP = 0; //Network Gateway IP address
unsigned char  g_ucConnectionSSID[SSID_LEN_MAX+1]; //Connection SSID
unsigned char  g_ucConnectionBSSID[BSSID_LEN_MAX]; //Connection BSSID

unsigned long  g_ulDestinationIp = IP_ADDR;        // Client IP address
unsigned int   g_uiPortNum = PORT_NUM; //Remote port number
unsigned long  g_ulIpAddr = 0; //The IP address of this microcontroller
char g_cBsdBuf[BUF_SIZE]; //The UDP receiver buffer

volatile int sockID = 0; //Socket ID for UDP

//Transimit of ultrasound variables
volatile unsigned int TIMER_INTERVAL_RELOAD = 1000;
unsigned int numberOfCycles = 1600;//Stores the count of ultrasound pulse cycles
unsigned int countToggles = 0; //Counts the number of pin toggles

volatile unsigned int SW2Flag = 0; //The state of the button (Either pressed or not)

volatile unsigned int room_id = 0; //The id of the room the node is in

// Receiver adc
unsigned int state = 0;
unsigned int ADC_ch;
unsigned long ADC_pin;

// ADC sample setting settings and
#define size 10640
unsigned int entry = 0;
long array[size];
float average = 0;
float firstav = 0;
unsigned int counter = 0;
unsigned int count = 0;

#if defined(ccs)
extern void (* const g_pfnVectors[])(void);
#endif
#if defined(ewarm)
extern uVectorEntry __vector_table;
#endif


//*****************************************************************************
//                 GLOBAL VARIABLES -- End
//*****************************************************************************


//****************************************************************************
//                      LOCAL FUNCTION PROTOTYPES
//****************************************************************************
static void InitializeAppVariables();
static void BoardInit(void);
static long ConfigureSimpleLinkToDefaultState(void);
signed int SmartConfigConnect(void);
long WpsConnectPushButton(void);

signed int setupWIFIReceiver(void);
signed int sendData(char data[],unsigned int dataLength);
signed int receiveData(void);

void SW2InterruptHandler(void);
void SW3InterruptHandler(void);

void SetupUltrasoundTxTimerPWMMode(unsigned long ulBase, unsigned long ulTimer,unsigned long ulConfig, unsigned char ucInvert);
void configureUltrasoundTxTimer(void);
static void ultrasoundTxTimerIntHandler(void);
void disableUltrasoundTimer(void);
void generateUltrasoundFrequency(void);

static void ReceiverADC(int first);
void Configureadc(void);

signed int performAction(char data[]);
int sendLocalIPAddress(void);
void showAndPerformError(void);


//*****************************************************************************
//
//! \brief This function initializes the application variables
//!
//! \param    None
//!
//! \return None
//!
//*****************************************************************************
static void InitializeAppVariables(){
    g_ulStatus = 0;
    g_ulGatewayIP = 0;
    memset(g_ucConnectionSSID,0,sizeof(g_ucConnectionSSID));
    memset(g_ucConnectionBSSID,0,sizeof(g_ucConnectionBSSID));
    g_ulDestinationIp = IP_ADDR;
    g_uiPortNum = PORT_NUM;
}
//*****************************************************************************
//
//! Board Initialization & Configuration
//!
//! \param  None
//!
//! \return None
//
//*****************************************************************************
static void BoardInit(void)
{
	/* In case of TI-RTOS vector table is initialize by OS itself */
	#ifndef USE_TIRTOS
    	//
    	// Set vector table base
    	//
		#if defined(ccs)
    		MAP_IntVTableBaseSet((unsigned long)&g_pfnVectors[0]);
		#endif
		#if defined(ewarm)
    		MAP_IntVTableBaseSet((unsigned long)&__vector_table);
		#endif
	#endif
    //
    // Enable Processor
    //
    MAP_IntMasterEnable();
    MAP_IntEnable(FAULT_SYSTICK);

    PRCMCC3200MCUInit();
}

//*****************************************************************************
// SimpleLink Asynchronous Event Handlers -- Start
//*****************************************************************************


//*****************************************************************************
//
//! \brief The Function Handles WLAN Events
//!
//! \param[in]  pWlanEvent - Pointer to WLAN Event Info
//!
//! \return None
//!
//*****************************************************************************
void SimpleLinkWlanEventHandler(SlWlanEvent_t *pWlanEvent){
    if(!pWlanEvent){
        return;
    }
    switch(pWlanEvent->Event){
        case SL_WLAN_CONNECT_EVENT:
        {
            SET_STATUS_BIT(g_ulStatus, STATUS_BIT_CONNECTION);

            //
            // Information about the connected AP (like name, MAC etc) will be
            // available in 'slWlanConnectAsyncResponse_t'
            // Applications can use it if required
            //
            //  slWlanConnectAsyncResponse_t *pEventData = NULL;
            // pEventData = &pWlanEvent->EventData.STAandP2PModeWlanConnected;
            //

            // Copy new connection SSID and BSSID to global parameters
            memcpy(g_ucConnectionSSID,pWlanEvent->EventData.
                   STAandP2PModeWlanConnected.ssid_name,
                   pWlanEvent->EventData.STAandP2PModeWlanConnected.ssid_len);
            memcpy(g_ucConnectionBSSID,
                   pWlanEvent->EventData.STAandP2PModeWlanConnected.bssid,
                   SL_BSSID_LENGTH);

            printf("[WLAN EVENT] STA Connected to the AP: %s , "
                       "BSSID: %x:%x:%x:%x:%x:%x\n\r",
                      g_ucConnectionSSID,g_ucConnectionBSSID[0],
                      g_ucConnectionBSSID[1],g_ucConnectionBSSID[2],
                      g_ucConnectionBSSID[3],g_ucConnectionBSSID[4],
                      g_ucConnectionBSSID[5]);
        }
        break;

        case SL_WLAN_DISCONNECT_EVENT:
        {
            slWlanConnectAsyncResponse_t*  pEventData = NULL;

            CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_CONNECTION);
            CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_IP_AQUIRED);

            pEventData = &pWlanEvent->EventData.STAandP2PModeDisconnected;

            // If the user has initiated 'Disconnect' request,
            //'reason_code' is SL_USER_INITIATED_DISCONNECTION
            if(SL_USER_INITIATED_DISCONNECTION == pEventData->reason_code)
            {
                printf("[WLAN EVENT]Device disconnected from the AP: %s, "
                           "BSSID: %x:%x:%x:%x:%x:%x on application's "
                           "request \n\r",
                           g_ucConnectionSSID,g_ucConnectionBSSID[0],
                           g_ucConnectionBSSID[1],g_ucConnectionBSSID[2],
                           g_ucConnectionBSSID[3],g_ucConnectionBSSID[4],
                           g_ucConnectionBSSID[5]);
            }
            else
            {
                printf("[WLAN ERROR]Device disconnected from the AP AP: %s, "
                           "BSSID: %x:%x:%x:%x:%x:%x on an ERROR..!! \n\r",
                           g_ucConnectionSSID,g_ucConnectionBSSID[0],
                           g_ucConnectionBSSID[1],g_ucConnectionBSSID[2],
                           g_ucConnectionBSSID[3],g_ucConnectionBSSID[4],
                           g_ucConnectionBSSID[5]);
            }
            memset(g_ucConnectionSSID,0,sizeof(g_ucConnectionSSID));
            memset(g_ucConnectionBSSID,0,sizeof(g_ucConnectionBSSID));
        }
        break;

        default:
        {
            printf("[WLAN EVENT] Unexpected event [0x%x]\n\r",
                       pWlanEvent->Event);
        }
        break;
    }
}

//*****************************************************************************
//
//! \brief This function handles network events such as IP acquisition, IP
//!           leased, IP released etc.
//!
//! \param[in]  pNetAppEvent - Pointer to NetApp Event Info
//!
//! \return None
//!
//*****************************************************************************
void SimpleLinkNetAppEventHandler(SlNetAppEvent_t *pNetAppEvent){
    if(!pNetAppEvent){
        return;
    }

    switch(pNetAppEvent->Event){
        case SL_NETAPP_IPV4_IPACQUIRED_EVENT:
        {
            SlIpV4AcquiredAsync_t *pEventData = NULL;

            SET_STATUS_BIT(g_ulStatus, STATUS_BIT_IP_AQUIRED);

            //Ip Acquired Event Data
            pEventData = &pNetAppEvent->EventData.ipAcquiredV4;

            //Local IP address
            g_ulIpAddr = pEventData->ip;

            //Gateway IP address
            g_ulGatewayIP = pEventData->gateway;

            printf("[NETAPP EVENT] IP Acquired: IP=%d.%d.%d.%d , "
                       "Gateway=%d.%d.%d.%d\n\r",
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,3),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,2),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,1),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,0),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,3),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,2),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,1),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,0));
        }
        break;

        default:
        {
            printf("[NETAPP EVENT] Unexpected event [0x%x] \n\r",
                       pNetAppEvent->Event);
        }
        break;
    }
}
//*****************************************************************************
//
//! \brief This function handles HTTP server events
//!
//! \param[in]  pServerEvent - Contains the relevant event information
//! \param[in]    pServerResponse - Should be filled by the user with the
//!                                      relevant response information
//!
//! \return None
//!
//****************************************************************************
void SimpleLinkHttpServerCallback(SlHttpServerEvent_t *pHttpEvent,SlHttpServerResponse_t *pHttpResponse){
    // Unused in this application
}

//*****************************************************************************
//
//! \brief This function handles General Events
//!
//! \param[in]     pDevEvent - Pointer to General Event Info
//!
//! \return None
//!
//*****************************************************************************
void SimpleLinkGeneralEventHandler(SlDeviceEvent_t *pDevEvent){
    if(!pDevEvent){
        return;
    }

    //
    // Most of the general errors are not FATAL are are to be handled
    // appropriately by the application
    //
    printf("[GENERAL EVENT] - ID=[%d] Sender=[%d]\n\n",
               pDevEvent->EventData.deviceEvent.status,
               pDevEvent->EventData.deviceEvent.sender);
}
//*****************************************************************************
//
//! This function handles socket events indication
//!
//! \param[in]      pSock - Pointer to Socket Event Info
//!
//! \return None
//!
//*****************************************************************************
void SimpleLinkSockEventHandler(SlSockEvent_t *pSock){
    if(!pSock){
        return;
    }

    //
    // This application doesn't work w/ socket - Events are not expected
    //
    switch( pSock->Event ){
        case SL_SOCKET_TX_FAILED_EVENT:
            switch( pSock->socketAsyncEvent.SockTxFailData.status){
                case SL_ECLOSE:
                    printf("[SOCK ERROR] - close socket (%d) operation "
                                "failed to transmit all queued packets\n\n",
                                    pSock->socketAsyncEvent.SockTxFailData.sd);
                    break;
                default:
                    printf("[SOCK ERROR] - TX FAILED  :  socket %d , reason "
                                "(%d) \n\n",
                                pSock->socketAsyncEvent.SockTxFailData.sd, pSock->socketAsyncEvent.SockTxFailData.status);
                  break;
            }
            break;

        default:
        	printf("[SOCK EVENT] - Unexpected Event [%x0x]\n\n",pSock->Event);
          break;
    }
}
//*****************************************************************************
// SimpleLink Asynchronous Event Handlers -- End
//*****************************************************************************

//*****************************************************************************
//! \brief This function puts the device in its default state. It:
//!           - Set the mode to STATION
//!           - Configures connection policy to Auto and AutoSmartConfig
//!           - Deletes all the stored profiles
//!           - Enables DHCP
//!           - Disables Scan policy
//!           - Sets Tx power to maximum
//!           - Sets power policy to normal
//!           - Unregister mDNS services
//!           - Remove all filters
//!
//! \param   none
//! \return  On success, zero is returned. On error, negative is returned
//*****************************************************************************
static long ConfigureSimpleLinkToDefaultState()
{
    SlVersionFull   ver = {0};
    _WlanRxFilterOperationCommandBuff_t  RxFilterIdMask = {0};

    unsigned char ucVal = 1;
    unsigned char ucConfigOpt = 0;
    unsigned char ucConfigLen = 0;
    unsigned char ucPower = 0;

    long lRetVal = -1;
    long lMode = -1;

    lMode = sl_Start(0, 0, 0);
    ASSERT_ON_ERROR(lMode);

    // If the device is not in station-mode, try configuring it in station-mode 
    if (ROLE_STA != lMode)
    {
        if (ROLE_AP == lMode)
        {
            // If the device is in AP mode, we need to wait for this event 
            // before doing anything 
            while(!IS_IP_ACQUIRED(g_ulStatus))
            {
				#ifndef SL_PLATFORM_MULTI_THREADED
            		_SlNonOsMainLoopTask();
				#endif
            }
        }

        // Switch to STA role and restart 
        lRetVal = sl_WlanSetMode(ROLE_STA);
        ASSERT_ON_ERROR(lRetVal);

        lRetVal = sl_Stop(0xFF);
        ASSERT_ON_ERROR(lRetVal);

        lRetVal = sl_Start(0, 0, 0);
        ASSERT_ON_ERROR(lRetVal);

        // Check if the device is in station again 
        if (ROLE_STA != lRetVal)
        {
            // We don't want to proceed if the device is not coming up in STA-mode 
            return DEVICE_NOT_IN_STATION_MODE;
        }
    }
    
    // Get the device's version-information
    ucConfigOpt = SL_DEVICE_GENERAL_VERSION;
    ucConfigLen = sizeof(ver);
    lRetVal = sl_DevGet(SL_DEVICE_GENERAL_CONFIGURATION, &ucConfigOpt, 
                                &ucConfigLen, (unsigned char *)(&ver));
    ASSERT_ON_ERROR(lRetVal);
    
    UART_PRINT("Host Driver Version: %s\n\r",SL_DRIVER_VERSION);
    UART_PRINT("Build Version %d.%d.%d.%d.31.%d.%d.%d.%d.%d.%d.%d.%d\n\r",
    ver.NwpVersion[0],ver.NwpVersion[1],ver.NwpVersion[2],ver.NwpVersion[3],
    ver.ChipFwAndPhyVersion.FwVersion[0],ver.ChipFwAndPhyVersion.FwVersion[1],
    ver.ChipFwAndPhyVersion.FwVersion[2],ver.ChipFwAndPhyVersion.FwVersion[3],
    ver.ChipFwAndPhyVersion.PhyVersion[0],ver.ChipFwAndPhyVersion.PhyVersion[1],
    ver.ChipFwAndPhyVersion.PhyVersion[2],ver.ChipFwAndPhyVersion.PhyVersion[3]);

    // Set connection policy to Auto + SmartConfig 
    //      (Device's default connection policy)
    lRetVal = sl_WlanPolicySet(SL_POLICY_CONNECTION, 
                                SL_CONNECTION_POLICY(1, 0, 0, 0, 1), NULL, 0);
    ASSERT_ON_ERROR(lRetVal);

    // Remove all profiles
    lRetVal = sl_WlanProfileDel(0xFF);
    ASSERT_ON_ERROR(lRetVal);

    //
    // Device in station-mode. Disconnect previous connection if any
    // The function returns 0 if 'Disconnected done', negative number if already
    // disconnected Wait for 'disconnection' event if 0 is returned, Ignore 
    // other return-codes
    //
    lRetVal = sl_WlanDisconnect();
    if(0 == lRetVal)
    {
        // Wait
        while(IS_CONNECTED(g_ulStatus))
        {
			#ifndef SL_PLATFORM_MULTI_THREADED
              _SlNonOsMainLoopTask(); 
			#endif
        }
    }

    // Enable DHCP client
    lRetVal = sl_NetCfgSet(SL_IPV4_STA_P2P_CL_DHCP_ENABLE,1,1,&ucVal);
    ASSERT_ON_ERROR(lRetVal);

    // Disable scan
    ucConfigOpt = SL_SCAN_POLICY(0);
    lRetVal = sl_WlanPolicySet(SL_POLICY_SCAN , ucConfigOpt, NULL, 0);
    ASSERT_ON_ERROR(lRetVal);

    // Set Tx power level for station mode
    // Number between 0-15, as dB offset from max power - 0 will set max power
    ucPower = 0;
    lRetVal = sl_WlanSet(SL_WLAN_CFG_GENERAL_PARAM_ID, 
            WLAN_GENERAL_PARAM_OPT_STA_TX_POWER, 1, (unsigned char *)&ucPower);
    ASSERT_ON_ERROR(lRetVal);

    // Set PM policy to normal
    lRetVal = sl_WlanPolicySet(SL_POLICY_PM , SL_NORMAL_POLICY, NULL, 0);
    ASSERT_ON_ERROR(lRetVal);

    // Unregister mDNS services
    lRetVal = sl_NetAppMDNSUnRegisterService(0, 0);
    ASSERT_ON_ERROR(lRetVal);

    // Remove  all 64 filters (8*8)
    memset(RxFilterIdMask.FilterIdMask, 0xFF, 8);
    lRetVal = sl_WlanRxFilterSet(SL_REMOVE_RX_FILTER, (_u8 *)&RxFilterIdMask,
                       sizeof(_WlanRxFilterOperationCommandBuff_t));
    ASSERT_ON_ERROR(lRetVal);

    lRetVal = sl_Stop(SL_STOP_TIMEOUT);
    ASSERT_ON_ERROR(lRetVal);

    InitializeAppVariables();
    
    return lRetVal; // Success
}

//*****************************************************************************
//
//! \brief Connecting to a WLAN Accesspoint using SmartConfig provisioning
//!
//! Enables SmartConfig provisioning for adding a new connection profile
//! to CC3200. Since we have set the connection policy to Auto, once
//! SmartConfig is complete, CC3200 will connect automatically to the new
//! connection profile added by smartConfig.
//!
//! \param[in]                     None
//!
//! \return                        None
//!
//! \note
//!
//! \warning                    If the WLAN connection fails or we don't
//!                             acquire an IP address, We will be stuck in this
//!                             function forever.
//
//*****************************************************************************
int SmartConfigConnect(void){
    unsigned char policyVal;
    long lRetVal = -1;

    lRetVal = sl_WlanProfileDel(WLAN_DEL_ALL_PROFILES);
    ASSERT_ON_ERROR(lRetVal);
    //Clear all profiles

    lRetVal = sl_WlanPolicySet(  SL_POLICY_CONNECTION,
                      SL_CONNECTION_POLICY(1,0,0,0,1),
                      &policyVal,
                      1 /*PolicyValLen*/);
    ASSERT_ON_ERROR(lRetVal);
    //set AUTO policy to enable connection upon smart config succeeding

    lRetVal = sl_WlanSmartConfigStart(0,                /*groupIdBitmask*/
                           SMART_CONFIG_CIPHER_NONE,    /*cipher*/
                           0,                           /*publicKeyLen*/
                           0,                           /*group1KeyLen*/
                           0,                           /*group2KeyLen */
                           NULL,                        /*publicKey */
                           NULL,                        /*group1Key */
                           NULL);                       /*group2Key*/
    ASSERT_ON_ERROR(lRetVal);
    // Start SmartConfig

    while((!IS_CONNECTED(g_ulStatus)) || (!IS_IP_ACQUIRED(g_ulStatus)))
    {
    	if(SW2Flag==1)
    		break;
    	//Breaks the loop only if SW2 is pressed where SW2Flag is then set to 1
        _SlNonOsMainLoopTask();
    }
    // Wait for WLAN Event

    if(SW2Flag==1){
    	//Entering WPS mode

    	lRetVal = sl_WlanSmartConfigStop();
    	//Stopped Smart config mode
    	if(lRetVal < 0){
    		//Closing of smart config failed
    		showAndPerformError();
    	}
    	//Stops smart config

   		lRetVal = WpsConnectPushButton();
    	if(lRetVal < 0){
    		//Connect through push button failed
    		showAndPerformError();
    	}
    	//Initiates WPS
    }
    //

     //
     // Turn ON the RED LED to indicate connection success
     //

    GPIO_IF_LedOn(MCU_RED_LED_GPIO);

    //wait for few moments
    //MAP_UtilsDelay(80000000);


    lRetVal = sl_WlanPolicySet(  SL_POLICY_CONNECTION,
                          SL_CONNECTION_POLICY(1,0,0,0,0),
                          &policyVal,
                          1 /*PolicyValLen*/);
    //reset to default AUTO policy and disable smart config policy
    ASSERT_ON_ERROR(lRetVal);

    return SUCCESS;
}
//*****************************************************************************
//
//!    \brief Connecting to a WLAN Accesspoint
//!    This function connects to the required AP (SSID_NAME).
//!    This code example assumes the AP doesn't use WIFI security.
//!    The function will return only once we are connected
//!    and have acquired IP address
//!
//!    \param[in] None
//!
//!    \return 0 on success else error code
//!
//!    \note
//!
//!    \warning    If the WLAN connection fails or we don't aquire an IP address,
//!                We will be stuck in this function forever.
//
//*****************************************************************************
long WpsConnectPushButton(){
	GPIO_IF_LedOn(MCU_RED_LED_GPIO);
	GPIO_IF_LedOn(MCU_GREEN_LED_GPIO);
	//Indicate that the button was pressed successfully

	SlSecParams_t secParams;
    long lRetVal = -1;

    secParams.Key = "";
    secParams.KeyLen = 0;
    secParams.Type = SL_SEC_TYPE_WPS_PBC;
    //Setting the security parameters for WPS

    lRetVal = sl_Start(NULL,NULL,NULL);
    if (lRetVal < 0 || ROLE_STA != lRetVal)
    {
        LOOP_FOREVER();
    }
    //Restart simplelink for use with WPS

    lRetVal = sl_WlanConnect(SSID_NAME, strlen(SSID_NAME), 0, &secParams,0);
    ASSERT_ON_ERROR(lRetVal);
    while((!IS_CONNECTED(g_ulStatus)) || (!IS_IP_ACQUIRED(g_ulStatus)))
    {
    	_SlNonOsMainLoopTask();
    }
    GPIO_IF_LedOff(MCU_RED_LED_GPIO);
    GPIO_IF_LedOff(MCU_GREEN_LED_GPIO);
    //Indicate success of connection via WPS
    return SUCCESS;
}
//****************************************************************************
//
//!    \brief Parse the input IP address from the user
//!
//!    \param[in]                     ucCMD (char pointer to input string)
//!
//!    \return                        0 : if correct IP, -1 : incorrect IP
//
//****************************************************************************
int IpAddressParser(char *ucCMD)
{
    int i=0;
    unsigned int uiUserInputData;
    unsigned long ulUserIpAddress = 0;
    char *ucInpString;
    ucInpString = strtok(ucCMD, ".");
    uiUserInputData = (int)strtoul(ucInpString,0,10);
    while(i<4)
    {
        //
       // Check Whether IP is valid
       //
       if((ucInpString != NULL) && (uiUserInputData < 256))
       {
           ulUserIpAddress |= uiUserInputData;
           if(i < 3)
               ulUserIpAddress = ulUserIpAddress << 8;
           ucInpString=strtok(NULL,".");
           uiUserInputData = (int)strtoul(ucInpString,0,10);
           i++;
       }
       else
       {
           return -1;
       }
    }
    g_ulDestinationIp = ulUserIpAddress;
    return SUCCESS;
}
//ALL CODE ABOVE ARE COPIED AND REQUIRED FOR OPERATION OF WIFI & ALL CODE BELOW ARE PROGRAMMED BY US

//THE NEXT THREE METHODS BELOW ARE FOR UDP COMMUNICATIONS///////////////////////////////////////////
/*
 * This method setups the receiver to bind to the address 0.0.0.0.
 * This enables it to receive from any device on the local network.
 * Note: No listen or accept is required as UDP is connectionless protocol
 */
signed int setupWIFIReceiver(void){
	SlSockAddrIn_t  sLocalAddr;
	int             iStatus;
	int             iAddrSize;
	sLocalAddr.sin_family = SL_AF_INET;
	sLocalAddr.sin_port = sl_Htons((unsigned short)PORT_NUM);
	sLocalAddr.sin_addr.s_addr = 0;
	//filling the UDP server socket address
	iAddrSize = sizeof(SlSockAddrIn_t);
	iStatus = sl_Bind(sockID, (SlSockAddr_t *)&sLocalAddr, iAddrSize);
	if( iStatus < 0 ){
	    sl_Close(sockID);
	    return FAILURE;
	    //In case of error
	}
	// binding the UDP socket to the UDP server address
	long iCounter =0;
	for (iCounter=0 ; iCounter<BUF_SIZE ; iCounter++){
		g_cBsdBuf[iCounter] = (char)(iCounter % 10);
	}
	// Fill the buffer completely
	return SUCCESS;
}
/*
 * This method transmits the specified data given that the a socket is already created.
 */
signed int sendData(char data[],unsigned int dataLength){
	SlSockAddrIn_t  sAddr;
	int             iAddrSize;
	int             iStatus;
	sAddr.sin_family = SL_AF_INET;
	sAddr.sin_port = sl_Htons((unsigned short)PORT_NUM);
	sAddr.sin_addr.s_addr = sl_Htonl((unsigned int)g_ulDestinationIp);
	//Fills the destination UDP socket address
	iAddrSize = sizeof(SlSockAddrIn_t);
	//Obtains the Size of the address
	iStatus = sl_SendTo(sockID, data, dataLength, 0,(SlSockAddr_t *)&sAddr, iAddrSize);
	if( iStatus <= 0 ){
		sl_Close(sockID);
		//Close port
	    return FAILURE;
	    //Return error
	}
	//Sends data to desired destination
	return SUCCESS;
}
/*
 * This method receives data from anyone given that the port is previously binded using setupWIFIReceiver().
 */
signed int receiveData(void){
    SlSockAddrIn_t  sAddr;
    int             iAddrSize;
    int             iStatus;
    long            sTestBufLen;

    sTestBufLen  = BUF_SIZE;
    //Initialise the sTestBufLen variable
    iAddrSize = sizeof(SlSockAddrIn_t);
    //Obtains the size of an IP address
    iStatus = sl_RecvFrom(sockID, g_cBsdBuf, sTestBufLen, 0,( SlSockAddr_t *)&sAddr, (SlSocklen_t*)&iAddrSize );
    if( iStatus < 0 ){
    	// error
    	sl_Close(sockID);
    	return FAILURE;
    }
    //Listen for data from any address
    signed int code = performAction(g_cBsdBuf);
    //Execute the actions (if any) specified in the payload of the received data packet

    if(code==-1){
    	sl_Close(sockID);
    	showAndPerformError();
    	//Received End message from hub which closes the socket
    }
    //Terminate the program gracefully if the command received was End.
    return SUCCESS;
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//THE NEXT TWO METHODS DEAL WITH SETTING UP THE SWITCHES FOR SWITCHING TO WPS & FOR MAKING THE HUB AWARE OF THIS UC///

/*
 * This method is the interrupt handler for button SW2 which simply changes the SW2Flag value
 * for getting out of the smart config while loop and switch to WPS
 */
void SW2InterruptHandler(){
	MAP_GPIOIntClear(GPIOA2_BASE,PIN_15);
	Button_IF_DisableInterrupt(SW2);
	//Clears the interrupt flag and disable interrupt as no longer needed for future operations
	SW2Flag = 1;
	//Sets the SW2Flag value for jumping out of the loop in smartConfigConnect()

	//Button_IF_EnableInterrupt(SW2);
	//Enable the button interrupt as it is disabled in the GPIO hander function in button_if.c
}
/*
 * This method is the interrupt handler for button SW3 which sends "Group Name" via WIFI
 */
void SW3InterruptHandler(){
	MAP_GPIOIntClear(GPIOA1_BASE,PIN_04);
	Button_IF_DisableInterrupt(SW3);
	//Clears the interrupt flag and disable the button whilst executing code

	long lRetVal = -1;
	//Used to determine if an error occured
	if(strcmp(STATE,"Band ")==0){
		lRetVal = sendLocalIPAddress();
		if(lRetVal < 0){
			showAndPerformError();
		}
		//Broadcast the local IP address by sending "Store Master/Slave Local IP_ADDRESS"
	}
	else{
		char data[] = "State Button_Pressed\n";
		unsigned int dataLength = sizeof(data)/sizeof(data[0]);
		//Creates the parameters required for sending Group Name
		lRetVal = sendData(data,dataLength);
		if(lRetVal < 0){
			showAndPerformError();
		}
		//Sends the data
	}
	Button_IF_EnableInterrupt(SW3);
	//Enable the button interrupt as it is disabled in the GPIO hander function in button_if.c
}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//THE NEXT THREE METHODS DEAL WITH SETTING UP THE HARDWARE BASED ULTRASOUND TX TIMER///////////////////////////////
/*
 * This method is COPIED and configures the settings of the timer in PWM mode
 */
void SetupUltrasoundTxTimerPWMMode(unsigned long ulBase, unsigned long ulTimer,unsigned long ulConfig, unsigned char ucInvert){

    MAP_TimerConfigure(ulBase,ulConfig);
    MAP_TimerPrescaleSet(ulBase,ulTimer,0);
    // Set GPT - Configured Timer in PWM mode.
    MAP_TimerControlLevel(ulBase,ulTimer,ucInvert);
    // Inverting the timer output if required
    MAP_TimerLoadSet(ulBase,ulTimer,TIMER_INTERVAL_RELOAD);
    // Load value set to ~0.5 ms time period
    MAP_TimerMatchSet(ulBase,ulTimer,TIMER_INTERVAL_RELOAD);
    // Match value set so as to output level 0
}
/*
 * This method is COPIED and configures the timer and enables interrupts
 */
void configureUltrasoundTxTimer(void){
    MAP_PRCMPeripheralClkEnable(PRCM_TIMERA3, PRCM_RUN_MODE_CLK);
    SetupUltrasoundTxTimerPWMMode(TIMERA3_BASE, TIMER_B,(TIMER_CFG_SPLIT_PAIR | TIMER_CFG_B_PWM), 1);

    MAP_TimerControlEvent(TIMERA3_BASE,TIMER_B,TIMER_EVENT_POS_EDGE);

    MAP_TimerIntClear(TIMERA3_BASE,TIMER_B);
    MAP_TimerIntRegister(TIMERA3_BASE,TIMER_B,ultrasoundTxTimerIntHandler);
    MAP_TimerIntEnable(TIMERA3_BASE, TIMER_CAPB_EVENT);
    //Sets up the interrupt
    HWREG(0x40033008) |= (1<<9);

    MAP_TimerEnable(TIMERA3_BASE,TIMER_B);
    MAP_TimerMatchSet(TIMERA3_BASE,TIMER_B,(127*DUTYCYCLE_GRANULARITY));
}
/*
 * This method defines the interrupt action of the timer
 */
static void ultrasoundTxTimerIntHandler(void){
	MAP_TimerIntClear(TIMERA3_BASE,TIMER_B);
	//Clear the interrupt flag
	countToggles++;
	//Increment the toggle count
	GPIO_IF_LedToggle(MCU_RED_LED_GPIO);
	//Generates the desired frequency by toggling a pin
}
/*
 * This method disables the timer corresponding to the creation of the ultrasound pulse
 */
void disableUltrasoundTimer(void){
	MAP_TimerIntDisable(TIMERA3_BASE, TIMER_CAPB_EVENT);
    MAP_TimerDisable(TIMERA3_BASE, TIMER_B);
}
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
 * The next two methods set up the ADC of the node/band to sample at 125 kHz
 * This is done by combining two ADC channels each sampling at their maximum
 * 62.5 kHz.
 */

void ReceiverADC(int first){
	Configureadc(); // Chooses the two ADC channels and enables them
	MAP_ADCEnable(ADC_BASE); // Enable ADC module
	int sample = 0; // This variable is used as the binary version of a captured sample.
	int value = 0; // This variable is used to store the sample converterd into a double.

	// Keep capturing samples until 85.12 ms worth of samples have been collected.
	while(counter < size) {
		// Making use of ADC as state machine to enable capture on two channels
		switch(state) {
			case 0:
				ADC_ch = ADC_CH_1;
	    	    ADC_pin = PIN_58;
	    	    break;
	    	case 1:
	    	  	ADC_ch = ADC_CH_3;
	    	   	ADC_pin = PIN_60;
	    	   	break;
	    }
		if(MAP_ADCFIFOLvlGet(ADC_BASE, ADC_ch)) { // If ADC has value to output
			sample = MAP_ADCFIFORead(ADC_BASE, ADC_ch); // Read value from ADC FIFO queue
		    array[counter] = sample; // Store sample in array

		    // Select other ADC channel
		    if(state == 0) state = 1;
		    else state = 0;
		    counter++;
		}
	}

	// If device is a slave then the simple detection of ultrasound is enough
	// This is why taking the average of the array of samples has been done here
	if (strcmp(STATE,"Slave ")==0) {
		for (entry = 0; entry < size; entry++) {
	       	value = (((array[entry] >> 2 ) & 0x0FFF)*1.4)/4096; // convert to double
	       	average = average*((entry-1)/entry) + value/entry; // Take average
		}

		// Record noise baseline
        if(first == 1) {
        	firstav = average;
        }

        // If average is twice greater than the noise baseline then ultrasound detected
        // Slave answers back via UDP unicast with "Group Heard ".
        else if(average >= 2*firstav) {
        	long lRetVal = -1;

    		char data[512]="";
    		char message [] = "Group Heard ";
    		char dot_ip[16];
   			char end[]="\n";

    		sprintf(dot_ip, "%d.%d.%d.%d",((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,3)),
    				((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,2)),
					((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,1)),
					((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,0)));
    		//Obtains IP address byte by byte then converts it to the equivalent decimal dotted notation and stores it in the array: dot_ip
    		strcat(data,message);
   			strcat(data,dot_ip);
   			strcat(data,end);
    		//Concatenates the command, state & IP address into the array: data
    		unsigned int dataSize = sizeof(data)/sizeof(data[0]);
   			lRetVal = sendData(data, dataSize);
    		if(lRetVal < 0){
   				ERR_PRINT(lRetVal);
   				LOOP_FOREVER();
   			}
    	}
    }

	// If device is a band then all samples are needed by the Hub for processing.
	// Since the sample array size is larger than a packet size then 76 packets are
	// needed to send all data.
	else if (strcmp(STATE,"Band ") == 0){
		long lRetVal = -1;

		// Creating 76 messages containing ADC samples
		int start = 0;
		int fin = 140;
		while (start < size){
			// the exact number of characters to be sen per packet if packet size is set to 1400
			char data[1286] = ""; // This is the maximum memory size that the MCU accepts.
			char message[] = "Triangulate ";
			char dot_ip[16];
			char end[]="\n\r";

			sprintf(dot_ip, "%d.%d.%d.%d ",((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,3)),
				((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,2)),
				((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,1)),
				((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,0)));
			//Obtains IP address byte by byte then converts it to the equivalent decimal dotted notation and stores it in the array: dot_ip
			strcat(data,message);
			strcat(data,dot_ip);
			int count1 = 0;
			for (count1 = start; count1 < fin; count1++) { // max is 3250
				if (count1 == size) break;
				char ADCsample[1]="";
				sprintf(ADCsample,"%f",(((array[count1] >> 2 ) & 0x0FFF)*1.4)/4096);
				//char ADCsample = (char) (((array[entry] >> 2 ) & 0x0FFF)*1.4)/4096;
				strcat(data, ADCsample);
				if(count1 < fin - 1){
					strcat(data, " ");
				}
			}
			strcat(data,end);
			//Concatenates the command, state & IP address into the array: data
			unsigned int dataSize = sizeof(data)/sizeof(data[0]);
			lRetVal = sendData(data, dataSize);
			if(lRetVal < 0){
				ERR_PRINT(lRetVal);
				LOOP_FOREVER();
			}
			start += 140;
			fin += 140;
		}
	}
	counter = 0;
	MAP_ADCDisable(ADC_BASE);
}

void Configureadc(void) {
	PinTypeADC(PIN_58,PIN_MODE_255); // Pinmux for the selected ADC input pin
	ADCChannelEnable(ADC_BASE, ADC_CH_1); // Enable ADC on selected channel
	PinTypeADC(PIN_60,PIN_MODE_255); // Pinmux for the selected ADC input pin
	ADCChannelEnable(ADC_BASE, ADC_CH_3); // Enable ADC on selected channel
}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//THE NEXT FOUR METHODS DESCRIBES ACTIONS THAT ARE TAKEN IN RESPONSE TO THE RECEIVED WIFI MESSAGE
/*
 * This method extracts the commands and compares against configured commands.
 * Next, if the command is recognised then executes the corresponding action
 */
signed int performAction(char data[]){
	char *split=strtok(data," ");
	//Get the first word of the message (where each word separated by space)
	if(strcmp(split,"Triangulate")==0){
		if(strcmp(STATE,"Band ")==0)
			ReceiverADC(0);
		else{
			split=strtok(NULL," ");
			//Get next word
			unsigned int received_room_id = atoi(split);
			if(room_id==received_room_id){
				generateUltrasoundFrequency();
			}
		}
	}
	else if(strcmp(split,"Configure")==0){
		split=strtok(NULL," ");
		//Get the next word which should be an integer
		TIMER_INTERVAL_RELOAD = atoi(split);
		//Extracts the integer part of the command and set as the timer interval reload
		//to generate a specific frequency
		split=strtok(NULL," ");
		//Get the next word which should be an integer
		room_id = atoi(split);
		//Extracts the second integer part of the command and set as the room_id
	}
	else if(strcmp(split,"Store")==0){
		split=strtok(NULL," ");
		//Get next word
		if(strcmp(split,"Hub")==0){
			split=strtok(NULL," ");
			//Get next word which should be the IP address
			IpAddressParser(split);
			//Set the IP address (Note that method handles the case where it
			//isnt valid)
		}
	}
	else if(strcmp(split,"State")==0){
		split=strtok(NULL," ");
		//Get the next word
		if(strcmp(split,"Ultrasound_Tx")==0){
			generateUltrasoundFrequency();
		}
		else if(strcmp(split,"Ultrasound_Rx")==0){
			ReceiverADC(0);
			//Perform ADC operations
		}
	}
	else if(strcmp(split,"End")==0){
		return -1;
		//For the hub to terminate the microcontroller program remotely
	}
	return 0;
	//For all cases except for END
}
/*
 * This method generates the ultrasound frequency with the desired number of cycles
 */
void generateUltrasoundFrequency(void){
	GPIO_IF_LedOff(MCU_RED_LED_GPIO);
	int numberOfToggles = numberOfCycles*2;
	configureUltrasoundTxTimer();
	while(countToggles<numberOfToggles){

	}
	GPIO_IF_LedOff(MCU_RED_LED_GPIO);
	//Transmits a specific number of cycles as specified in global variable
	disableUltrasoundTimer();
	countToggles=0;
	//Stop the timer and reset the count of the cycles
}
/*
 * This method simply sends "Store Master/Slave LOCAL IP_ADDRESS" given that a socket has already been created
 */
int sendLocalIPAddress(void){

	long lRetVal = -1;
	//Manage error returns

	char data[512]="";
	//Stores the final message that is to be sent
	char command[] = "Store ";
	char state[]=STATE;
	char dot_ip[16];
	char end[]="\n";
	//Each part of the message that is to be sent
	sprintf(dot_ip, "%d.%d.%d.%d",((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,3)),
			((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,2)),
			((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,1)),
			((unsigned char)SL_IPV4_BYTE(g_ulIpAddr,0)));
	//Obtains IP address byte by byte then converts it to the equivalent decimal dotted notation
	//stores it in the array: dot_ip
	strcat(data,command);
	strcat(data,state);
	strcat(data,dot_ip);
	strcat(data,end);
	//Concatenates the command, state & IP address into the array: data
	unsigned int dataSize = sizeof(data)/sizeof(data[0]);
	//Obtains the length of the array

	lRetVal = sendData(data,dataSize);
	if(lRetVal < 0){
		showAndPerformError();
	}
	//Send the data

	return SUCCESS;
	//Initiates a transmission
}
/*
 * This method causes the microcontroller to go into an infinite loop and informs the user
 * that an error has occured by displaying an LED
 */
void showAndPerformError(void){
	GPIO_IF_LedOn(MCU_GREEN_LED_GPIO);
	LOOP_FOREVER();
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
int main(void){
    long lRetVal = -1;
    //Manage error returns

    BoardInit();
    // Initialize Board configurations

    PinMuxConfig();
    // Configure the pinmux settings for the peripherals exercised

    GPIO_IF_LedConfigure(LED1|LED3);
    // configure RED & GREEN LED for usage

    InitializeAppVariables();
    //Initialise the variables associated with this program

    Button_IF_Init(SW2InterruptHandler,SW3InterruptHandler);
    //Configure button interrupt and enable it

    lRetVal = ConfigureSimpleLinkToDefaultState();
    if(lRetVal < 0)
    {
    	showAndPerformError();
    }
    //Device is configured in default state;

    CLR_STATUS_BIT_ALL(g_ulStatus);
    lRetVal = sl_Start(0,0,0);
    if (lRetVal < 0 || ROLE_STA != lRetVal)
    {
    	showAndPerformError();
    }
    //Start simplelink and start as a Station

    GPIO_IF_LedOn(MCU_RED_LED_GPIO);
    GPIO_IF_LedOn(MCU_GREEN_LED_GPIO);
    MAP_UtilsDelay(80000000);
    GPIO_IF_LedOff(MCU_RED_LED_GPIO);
    GPIO_IF_LedOff(MCU_GREEN_LED_GPIO);
    //Flash the LEDs to specify that the device is on and ready

    lRetVal = SmartConfigConnect();
    if(lRetVal < 0)
    {
    	showAndPerformError();
    }
    //Connect to our AP using SmartConfig method

    sockID = sl_Socket(SL_AF_INET,SL_SOCK_DGRAM, 0);
    if( sockID < 0 ){
    	showAndPerformError();
    }
    // create a UDP socket

    lRetVal = setupWIFIReceiver();
    if(lRetVal < 0){
    	showAndPerformError();
    }
    //Setup wifi for receiving data

    if(strcmp(STATE,"Slave ")==0){
    	//ReceiverADC(1);
    // Set threshold value for slave's ADC
    }
    if(strcmp(STATE,"Band ")!=0){
    	lRetVal = sendLocalIPAddress();
    	if(lRetVal < 0){
		   showAndPerformError();
    	}
    	//Broadcast the local IP address by sending "Store Master/Slave Local IP_ADDRESS"
    }//If it is any object that isnt a band
    while(1){
    	lRetVal = receiveData();
    	if(lRetVal < 0){
    		showAndPerformError();
    	}
    }//Continue forever receiving data

    //
}
