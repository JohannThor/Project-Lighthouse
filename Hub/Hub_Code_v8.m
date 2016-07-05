% Author: M. Yahia Al Kahf, June 2016

% This is the coordiantor program for the UKFC/Cities Unlocked
% indoor triangulation project.
% It needs to be located in the same file as the Functionalities M-file

% Get hub ip address
% First line outputs the host name and ip address
address = java.net.InetAddress.getLocalHost;
% Only read ip address
ipv4 = char(address.getHostAddress);

% Temporary hardcoding node positions and grid dimentions
% Once map of rooms is established then these distances should
% become inputs
nodesPos = [0 1.8 0;
            0 0 4.1];
% The room's diagonal distance
maxTargetDist = 4.5;

% Setup databases in shape of maps
SensorMap = containers.Map('KeyType', 'double', 'ValueType', 'any');
% SensorList entry:
% <GroupName> / <Device> + <Device no> / <ip address>
RoomMap = containers.Map('KeyType', 'double', 'ValueType', 'any');
% RoomList entry:
% <RoomName> / <Master ip address> / <Number of nodes in room>
BandMap = containers.Map('KeyType', 'double', 'ValueType', 'any');
% BandList entry:
% <BandName> / <Device> + <Device no> / <ip address> / <LastPos>

% number of masters in the system
mcount = 0;
% number of slaves in the system
scount = 0;
% number of devices (master + slaves) in the system
devcount = 0;
% number of bands in the system
bcount = 0;
% number of rooms in the system
rcount = 0;

% data array size
size = 10640;

% remote port which is going to be fixed for all communications
rport = 8050;
% local ports which are used for communication with various devices
lport = 8050;
mport = 8051;
sport1 = 8052;
sport2 = 8053;
pport = 8054;
baport = 8055;
dport = 8056;
brport = 8060;

% Setup UDP connection and open it
HubUDP = udp('0.0.0.0', rport, 'LocalPort', lport, 'Timeout', inf); 
fopen(HubUDP);

while 1
    % continously read on the udp port
    message = fscanf(HubUDP);
    % retracts command word from rest of message
    [command, rest] = strtok(message);
    
    % Compare command word with possible keywords 
    discover = strcmp(command, 'Discover');
    store = strcmp(command, 'Store');
    group = strcmp(command, 'Group');
    name = strcmp(command, 'Name');
    state = strcmp(command, 'State');
    triangulate = strcmp(command, 'Triangulate');
    sync = strcmp(command, 'Sync');
    ended = strcmp(command, 'End');
   
    % command Discover has been received
    if(discover == 1)
        % extract discover target device and compare with keyword "hub"
        [device, rest] = strtok(rest);
        hub = strcmp(device, 'Hub');
        
        if(hub == 1)
            % if "Discover Hub" has been received then the hub must 
            % respond with its own ip address
            disp('Received Discover Hub');
            [dipv4, trash] = strtok(rest);
            sent = Functionalities.Store_Hub(dipv4, rport, dport, ipv4);
        end
        
    % command Store has been received
    elseif(store == 1)
        % extract sender's nature and compare with possible keywords
        [param, rest] = strtok(rest);
        master = strcmp(param, 'Master');
        slave = strcmp(param, 'Slave');
        band = strcmp(param, 'Band');
        rname = strcmp(param, 'Room_Name');
        bname = strcmp(param, 'Band_Name');

        % message sent by a master
        if(master == 1)
            % extract master's ip address
            [dipv4, trash] = strtok(rest);
            disp('Received Store Master');
            % Hub responds with its own ip address
            sent = Functionalities.Store_Hub(dipv4, rport, mport, ipv4);
            disp('Sent Store Hub to master');
            
                                    %%%%%% Update database with newly received device %%%%%%
                                    
            % preparing new list entry
            value = {'GroupName' ['Master ' num2str(mcount + 1)] num2str(dipv4)};
            
            % If database is empty no need for duplicate check
            if(devcount == 0)
                [SensorMap, mcount, devcount] = Functionalities.Add_Master(value, SensorMap, mcount, devcount);
                
            else
                % Check if device has already been stored
                dup = Functionalities.Dup_Check(dipv4, SensorMap, devcount);
                
                % If duplicate check is passed then device is added to
                % SensorList
                if(dup == 0)
                    [SensorMap, mcount, devcount] = Functionalities.Add_Master(value, SensorMap, mcount, devcount);
                end
            end
                                                            %%%%%%
            
            % Display updated SensorList
            for q = 1:devcount
                disp(SensorMap(q));
            end
            
       % message sent by a slave   
       elseif(slave == 1)
           % extract slave's ip address
           [dipv4, trash] = strtok(rest);
           disp('Received Store Slave');
           % Hub responds with its own ip address
            sent = Functionalities.Store_Hub(dipv4, rport, sport1, ipv4);
            disp('Sent Store Hub to slave');
          
                                    %%%%%% Update database with newly received device %%%%%%
           % preparing new list entry                         
           value = {'GroupName' ['Slave ' num2str(scount + 1)] num2str(dipv4)};
           
           % If database contains no slaves no need for duplicate check
           if(scount == 0)
               [SensorMap, scount, devcount] = Functionalities.Add_Slave(value, SensorMap, scount, devcount);
               
           else
               % Check if device has already been stored
               dup = Functionalities.Dup_Check(dipv4, SensorMap, devcount);
               
               % If duplicate check is passed then device is added to
               % SensorList
               if(dup == 0)
                   [SensorMap, scount, devcount] = Functionalities.Add_Slave(value, SensorMap, scount, devcount);
               end
           end
                                                            %%%%%%
           
           % Display updated SensorList    
           for q = 1:devcount
               disp(SensorMap(q));
           end
        
        % message sent by a slave
        elseif(band == 1)
            % extract type of band info received
            [type, rest] = strtok(rest);
            [test, trash] = strtok(rest);
            % if info was only to store band
            if (isempty(trash))
                dipv4 = type;
                disp('Received Store Band');
                
                % Hub responds with its own ip address
                sent = Functionalities.Store_Hub(dipv4, rport, baport, ipv4);
                disp('Sent Store Hub to band');
           
                                    %%%%%% Update database with newly received device %%%%%%
                % preparing new list entry                         
                value = {'BandName' 'LastPos'};
           
                % If database contains no bands no need for duplicate check
                if(bcount == 0)
                    [BandMap, bcount] = Functionalities.Add_Band(dipv4, value, BandMap, bcount);
           
                else
                    % Check if device has already been stored
                    dup = Functionalities.Dup_Check(dipv4, BandMap);
               
                    % If duplicate check is passed then device is added to
                    % BandList
                    if(dup == 0)
                        [BandMap, bcount] = Functionalities.Add_Band(dipv4, value, BandMap, bcount);
                    end
                end
                
            % if info was the name of the stored band
            else
                name = type;
                dipv4 = test;
                
                disp('Received Band Name');
                % Rename band to newly assigned name given by phone
                [newname, BandMap] = Functionalities.Change_BName(dipv4, name, BandMap, bcount);
            
                % compare band name with name given by the phone
                comp = strcmp(name, newname);
                % send response on naming status of band to phone
                sent =  Functionalities.Phone_Reply3(comp, rport, brport);
            end
                                                            %%%%%%
                                                                       
           % Display updated BandList    
           for q = 1:bcount
               disp(BandMap(q));
           end
        
        % Room name sent by the phone
        elseif(rname == 1)
            % extract room's given name
            [name, trash] = strtok(rest);
            % Change name of node group to given room name
            [SensorMap, RoomMap] = Functionalities.Change_RName(key, ripv4, SensorMap, RoomMap);
            % Tell phone of process completion
            sent = Functionalities.Phone_Reply2(rport, brport);
            
            % Display updated SensorList
            for q = 1:devcount
                disp(SensorMap(q));
            end
        end
            
    % command Group has been received
    elseif(group == 1)
        % extract message info and compare with keyword "Nodes"
        [info, trash] = strtok(rest);
        nodes = strcmp(info, 'Nodes');
        
        % This command comming from the phone initiates the grouping of the
        % devices
        if(nodes == 1)
            disp('Received Group Nodes');
            % send ultrasound triggers to masters and slaves by asking
            % masters to send one by one and slaves to listen and report
            % back for each master transmission
            for c = 1:mcount
                % 1. retrieve master info from database 
                % 2. extract master's ip address
                % 3. change group name
                % 4. update RoomList
                mInfo = SensorMap(c);
                mipv4 = mInfo{4};
                mInfo(1) = {['Group ' num2str(c)]};
                SensorMap(c) = mInfo;
                rcount = rcount + 1;
                entry = {mInfo(1) mInfo(4) ['Room ' num2str(rcount)]};
                RoomMap(c) = entry;
               
                % Tell all slaves to send ultrasound 
                sent = Functionalities.Slaves_Us_Rx(SensorMap, mcount, devcount, rport, sport1);
                disp(sent);
               
                % Tell master to emmit ultrasound
                sent = Functionalities.Master_Us_Tx(mipv4, rport, mport);
                disp(sent);
                
                % Slaves will answer back very quickly so to capture all
                % responses a timeout of 2 seconds will be used on the udp
                % port.
                % Once it is reached then it is safe to assume that all
                % slaves that have heard the master have been grouped.
                
                % Create new udp port with timeout
                fclose(HubUDP);
                HubUDP2 = udp('0.0.0.0', rport, 'LocalPort', lport, 'Timeout', 2);
                fopen(HubUDP2);
                % Clear variable message
                message = 0;
                
                % while timout has not been reached the while loop will
                % continuously scan
                while (message ~= '')
                    message = fscanf(HubUDP2);
                    % extract command from message and compare it to
                    % keyword 'Group Heard'
                    [command, rest1] = strtok(message);
                    [info, rest2] = strtok(rest1);
                    heard = strcmp([command, ' ', info], 'Group Heard');
                    
                    % Received 'Group Heard' from slave
                    if(heard == 1)
                        % Extract slave ip address
                        [dipv4, trash] = strtok(rest2);
                        % group master and slave
                        SensorMap = Functionalities.Group_Slave(c, dipv4, SensorMap, mcount, devcount);
                    end
                end
                % Remove timeout
                fclose(HubUDP2);
                fopen(HubUDP);
            end
            
            % Infrom the phone that node grouping has been completed
            sent = Functionalities.Group_Formed(rport, brport);
            
            % Display updated SensorList
            for q = 1:devcount
                disp(SensorMap(q));
            end
        end
   
    % Codeword State has been received
    elseif(state == 1)
        % extract interface of which the state is a concern and the
        % sender's ip address
        [interface, rest2] = strtok(rest1);
        [ripv4, trash] = strtok(rest2);
        % compare interface with keyword 'Button_Pressed'
        button = strcmp(interface, 'Button_Pressed');
        
        % if grouping button on node has been pressed then it is time to
        % name the group to which this node belongs
        if(button == 1)
            disp('Received State Button_Pressed');
            [key, ngroup] = Functionalities.Find_Node(ripv4, SensorMap, devcount);
            
            % Inform phone of node status, ask for group name if status
            % verified
            sent = Functionalities.Phone_Reply1(key, ngrouped, rport, brport);
        end
        
    % Codeword Triangulate has been received
    elseif (triangulate == 1)
        % found flag reset
        found = 0;
        % extracts device type and name
        [info, rest] = strtok(rest);
        
        % received triangulate command from phone
        if(strcmp(info, 'Band'))
            [name, trash] = strtok(rest);
        
            % Look for targeted band in BandList
            targetinfo = Functionalities.Identify_Target(name, BandMap, bcount);
            dipv4 = targetinfo(3);
        
            % If band position has already been determined then best place to
            % look for it again would be its last position
            if(~strcmp('LastPos', targetinfo{4}))
                % Find appropriate nodes to activate.
                [mInfo, s1Info, s2Info] = Functionalities.Identify_Nodes(targetinfo, SensorMap, RoomMap, mcount, devcount);
                % Activate found nodes and band to send or capture ultrasound
                sent = Functionalities.Activate_Devices(mInfo, s1Info, s2Info, dipv4);
                % Look for band response
                bmessage = fscanf(HubUDP);
                % Band has heard ultrasound and has sent ADC samples
                if(~strcmp(bmessage, 'Group Not Heard'))
                    % set found flag
                    found = 1;
                    % extract command, band ip and ADC data
                    [command, rest] = strtok(bmessage);
                    [bipv4, data] = strtok(rest);
                    % split data into array of individual samples
                    samples = strsplit(data, ';');
                    % create an array of zeros that has the same size as
                    % the data array
                    numdata = zeros(1,size);
                    % convert samples from string to doubles
                    for o = 1:size
                        numdata(o) = str2double(samples{o});
                    end
                    if(~max(numdata) >= 1.4)
                        % Agree on how to acquire node positions
                        % Agree on how to acquire grid dimensions
                        % Call TDOA function
                        [BandPos] = TDOA(rInfo(3), nodesPos, maxTargetDist, numdata);
                        if(~isnan(BandPos))
                            found = 1;
                        end
                    % Clipping has occured, Band has been found without
                    % need for TDOA (because cross-corelation will not
                    % return anything)
                    % Inform phone of band position
                    else
                        found = 1;
                    end
                end
            end
            
            for c = 1:mcount
                % Stop looping if band position has been triangulated
                if(found == 1)
                    break
                end
                % If last position was already looked at then skip that
                % room
                rInfo = RoomMap(c);
                if (strcmp(rInfo(1), targetinfo{4}))
                    continue
                end
                % Find appropriate nodes to activate.
                [mInfo, s1Info, s2Info] = Functionalities.Identify_Nodes(targetinfo, SensorMap, RoomMap, mcount, devcount);
                % Activate found nodes and band to send or capture ultrasound
                sent = Functionalities.Activate_Devices(mInfo, s1Info, s2Info, dipv4);
                
                data = [];
                bmessage = fscanf(HubUDP); % collect data from band
                for packetnum = 1:76 % deal with individual packets
                    input = fscanf(HubUDP2); % acquire data array
                    split = strsplit(input, ' '); % split array into array of individual samples
                    command = split(1:2); % extract message
                    samples = split(3:end); % get rid of commands
                    sampleschar = char(samples); % transform to characters
                    output = transpose(str2num(sampleschar)); % transform to numbers
                    data = [data, output];
                end
                if(~max(data) >= 1.4)
                    % Agree on how to acquire node positions
                    % Agree on how to acquire grid dimensions
                    % Call TDOA function
                    [BandPos] = TDOA(rInfo(3), nodesPos, maxTargetDist, data);
                    if(~isnan(BandPos))
                        found = 1;
                    end
                % Clipping has occured, Band has been found without
                % need for TDOA (because cross-corelation will not
                % return anything)
                % Inform phone of band position
                else
                    found = 1;
                end
            end
        end
        % Inform phone of band position
        if (found == 1)
            sent = Functionalities.Phone_Reply4(bInfo(1), rinfo(1), rport, brport);
        end
        
    % Phone requests syncing of the lists with the hub
    % hub replies with list elements one by one
    elseif(sync == 1)
        [listname, trash] = strtok(rest);
        if(strcmp(listname, 'Band_List'))
            for q = 1:bcount
                info = BandMap(q);
                sent = Functionalities.Phone_Reply([BandMap(q), '; '], rport, brport);
            end
        elseif(strcmp(listname, 'Room_List'))
            for q = 1:rcount
                info = RoomMap(q);
                sent = Functionalities.Phone_Reply([RoomMap(q), '; '], rport, brport);
            end
        end
        
    % If "End" is received then scanning of HubUDP is terminated and
    % program exits while loop
    elseif(ended == 1)
        fclose(instrfind);
        break
    end
end