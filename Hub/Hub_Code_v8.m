% Author: M. Yahia Al Kahf, June 2016

classdef Functionalities_V2
    
    methods (Static)
        
        % This function will send "Store Hub <ip>" to the devices that 
        % request the hub's ip address
        function out = Store_Hub(dipv4, rport, lport, ipv4) 
            cast = udp(dipv4, rport, 'LocalPort', lport);
            fopen(cast);
            pause(2);
            message = ['Store Hub ' num2str(ipv4) ' '];
            fprintf(cast, message);
            out = 'Sent Store Hub';
            fclose(cast);
        end
        
        % This function will check if the received device info has not 
        % already been stored
        % It outputs "1" if duplicate detected
        function out = Dup_Check(dipv4, Map, count)
            for c = 1:count
                info = Map(c);
                dup = strcmp(info(3), dipv4);
                if (dup == 1)
                    break
                end
            end
            out = dup;
        end
        
        % This function adds a master to SensorList
        % It either stores the new master as the first element of the list
        % or add it in between the last master and the first slave
        function [SensorMap, mcount, devcount] = Add_Master(value, SensorMap, mcount, devcount)
            % If list is empty
            if(devcount == 0)
                % increase device and master counters
                devcount = devcount + 1;
                mcount = mcount + 1;
                % create a new device entry and use devcount as map key to
                % that entry
                SensorMap(devcount) = value;
            % if list contains saved devices
            else
                % Shift down existing slaves to make space for new master
                counter = 0;
                for c = mcount+1:devcount
                    q = devcount - counter;
                    SensorMap(q+1) = SensorMap(q);
                    counter = counter + 1;
                end
                % increase device and master counters
                devcount = devcount + 1;
                mcount = mcount + 1;
                % create a new device entry and use new mcount as map key 
                % to that entry
                SensorMap(mcount) = value;
            end
        end
        
        % This function adds a slave to SensorList
        % It stores the new slave as the last element of the list
        function [SensorMap, scount, devcount] = Add_Slave(value, SensorMap, scount, devcount)
            % increase device and slave counters
            devcount = devcount + 1;
            scount = scount + 1;
            % create a new device entry and use new devcount as map key 
            % to that entry
            SensorMap(devcount) = value;
        end
        
        % This function adds a band to BandList
        % It stores the new band as the last element of the list
        function [BandMap, bcount] = Add_Band(ipv4, value, BandMap, bcount)
            % increase band counter
            bcount = bcount + 1;
            % create a new device entry and use new bcount as map key 
            % to that entry
            BandMap(ipv4) = value;
        end
        
        % This function tells all slaves to listen to ultrasound
        function out = Slaves_Us_Rx(SensorMap, mcount, devcount, rport, sport1)
            % Go through all slaves
            for p = mcount+1:devcount
                % extract slave ip
                sInfo = SensorMap(p);
                sipv4 = sInfo{4};
                % communicate to slave and close port when done
                sUDP = udp(sipv4, rport, 'LocalPort', sport1); 
                fopen(sUDP);
                fprintf(sUDP, 'State Ultrasound_Rx ');
                fclose(sUDP);
                out = ('Sent State Ultrasound_Rx to slave');
            end
        end
        
        % This functions tells the selected master to transmit ultrasound
        function out = Master_Us_Tx(mipv4, rport, mport)
            mUDP = udp(mipv4, rport, 'LocalPort', mport);  
            fopen(mUDP);
            fprintf(mUDP, 'State Ultrasound_Tx ');
            fclose(mUDP);
            out = ['Sent State Ultrasound_Tx to master ', mipv4];
        end
        
        % This function will look into SensorList for the slave that has
        % responded to the master's ultrasound using the device's ip
        % address and will group that device with the master once found
        % within the list
        function SensorList = Group_Slave(c, dipv4, SensorList, mcount, devcount)
            % extract ip address of each slave
            for o = mcount+1:devcount
                info = SensorList(o);
                sipv4 = info{4};
                % compare extraced ip address with received one
                comp = strcmp(dipv4, sipv4);
                
                % matching slave's key in the list is saved
                if(comp == 1)
                    key = o;
                    break
                end
            end
            if (key ~= 0)
                % Replace the group of matching slave with the group
                % assigned to the master
                info = SensorList(key);
                info(1) = {['Group ' num2str(c)]};
                SensorList(key) = info;
            end
        end
        
        % This function tells the phone via broadcast that node grouping
        % hsa been completed
        function out = Group_Formed(rport, brport)
            bcast = udp('255.255.255.255', rport, 'LocalPort', brport);
            fopen(bcast);
            fprintf(bcast, 'Group Formed');
            fclose(bcast);
            out = 'Sent Group Formed';
        end
        
        % This function compares the ip of the nodes to the ip of the
        % device that sent the state message. If a match is found then
        % the key to the matching node in the list is saved and the 
        % not grouped flag is off
        function [key, ngroup] = Find_Node(dipv4, SensorList, devcount)
            % set flags to initial values
            key = 0;
            ngroup = 1;
            % go through all nodes in list, extract their ip addresses and
            % compare these with sender's ip
            for r = 1:devcount
                info = SensorList(r);
                ipv4 = info{4};
                comp = strcmp(dipv4, ipv4);
                % if match is found then save key to node
                if(comp == 1)
                    key = r;
                    info = SensorList(key);
                    % make sure that node belongs to previously formed
                    % group
                    ngroup = strcmp(info{1}, 'GroupName');
                    % node found, no need to continue looking
                    break
                end
            end
        end
        
        % This function answer the phone with an expected reply fed from
        % the main code Hub_Code
        function out =  Phone_Reply(message, rport, brport)
            bcast = udp('255.255.255.255', rport, 'LocalPort', brport);
            fopen(bcast);
            fprintf(bcast, message);
            out = ['Sent ', message, ' to phone'];
        end
        
        % This function communicates, to the phone via broadcast, a name
        % request if node status has been verified or a status report if
        % status has not been verified
        function out =  Phone_Reply1(key, ngrouped, rport, brport)
            bcast = udp('255.255.255.255', rport, 'LocalPort', brport);
            fopen(bcast);
            
            % If node is found and is grouped
            if (key ~= 0 && ngrouped == 0)
                fprintf(bcast, 'Discover Room_Name');
                out = 'Sent Discover Room_Name';
            % If node is not grouped
            elseif(key ~= 0 && ngrouped == 1)
                fprintf(bcast, 'Node Not Grouped');
                out = 'Sent Node Not Grouped';
            % If node is not found
            elseif(key == 0)
                fprintf(bcast, 'Node Not Found');
                out = 'Sent Node Not Found';
            end
            fclose(bcast);            
        end
        
        % This function informs the phone that the group selected has been
        % named
        function out =  Phone_Reply2(rport, brport)
            bcast = udp('255.255.255.255', rport, 'LocalPort', brport);
            fopen(bcast);
            fprintf(bcast, 'Group Named');
            fclose(bcast);
            out = 'Sent Group Named';
        end
        
        % This function either informs the phone that the band has been
        % renamed or coomunicates its old name
        function out =  Phone_Reply3(comp, rport, brport)
            bcast = udp('255.255.255.255', rport, 'LocalPort', brport);
            fopen(bcast);
            if (comp == 1)
                fprintf(bcast, 'Band Named');
                out = 'Sent Band Named';
            else
                fprintf(bcast, ['Band_Name Failed', name]);
                out = 'Sent Old Band Name';
            end
            fclose(bcast);
        end
        
        function out =  Phone_Reply4(bname, rname, rport, brport)
            bcast = udp('255.255.255.255', rport, 'LocalPort', brport);
            fopen(bcast);
            fprintf(bcast, ['Triangulate' bname, rname]);
            out = 'Sent Band Location';
            fclose(bcast);
        end
        
        function [SensorList, RoomList] = Change_RName(key, SensorList, RoomList)
            % extract group name of node that had its button pressed
            info = SensorList(key);
            curname = info{1};
            
            % Change group name of nodes that adhere to same group as
            % pressed node
            for q = 1:devcount
                info = SensorList(q);
                % compare group name of node to group name of pressed node
                % and change name to given one if comparison checks out
                if(strcmp(curname, info{1}))
                    info{1} = name;
                    SensorList(q) = info;
                end
            end 
            
            % Update RoomList with new name
            for q = 1:mcount
                info = RoomList(q);
                % change name of group once it has been found in the list
                if(strcmp(curname, info{1}))
                    info{1} = name;
                    RoomList(q) = info;
                end
            end
        end
        
        % This function either renames the band of interest or outputs its
        % current name
        function [newname, BandList] = Change_BName(dipv4, name, BandList, bcount)
            % Loops through bands
            for q = 1:bcount
                % extracts exisitng name
                info = BandList(q);
                % finds band of interest
                if(strcmp(dipv4, info{3}))
                    % sees whether band has already been named
                    if(strcmp('BandName', info{1}))
                        % if not then gives it a new name
                        info{1} = name;
                        % updates BandList
                        BandList(q) = info;
                        newname = name;
                        break
                    % if yes then outputs old name
                    else
                        newname = info{1};
                    end
                end
            end
        end
        
        % This function looks for the targertted band and outputs its
        % properties
        function targetinfo = Identify_Target(name, BandList, bcount)
            % Loops through list until target is found
            for q = 1:bcount
                info = BandList(q);
                if(strcmp(name, info{1}))
                    % target = q;
                    % info of interest is stored
                    targetinfo = info;
                    % target found, no need to keep looking
                    break
                end
            end
        end
            
        % This function identifies the nodes present in the target room
        function [mInfo, s1Info, s2Info] = Identify_Nodes(targetinfo, SensorList, mcount, devcount)
            % Loop through masters until the one located in the target room
            % is found
            for c1 = 1:mcount
                mInfo = SensorList(c1);
                if(strcmp(targetinfo{4}, mInfo{1}))
                    break
                end
            end
            % Loop through slaves until one located in the target room is
            % found
            for c2 = mcount+1:devcount
                s1Info = SensorList(c2);
                if(strcmp(targetinfo{4}, s1Info{1}))
                    break
                end
            end
            % Loop through slaves until the second one in the target room
            % is found
            for c3 = c2+1:devcount
                s2Info = SensorList(c3);
                if(strcmp(targetinfo{4}, s2Info{1}))
                    break
                end
            end
        end
        
        % This function tells the nodes to trasnmit ultrasound and the band
        % to listen to their transmissions
        function out = Activate_Devices(mInfo, s1Info, s2Info, dipv4)
            mUDP = udp(mInfo(4), rport, 'LocalPort', mport);
            s1UDP = udp(s1Info(4), rport, 'LocalPort', sport1);
            s2UDP = udp(s2Info(4), rport, 'LocalPort', sport2);
            baUDP = udp(dipv4, rport, 'LocalPort', baport);
            
            fprintf(baUDP, 'State Ultrasound_Rx '); 
            fprintf(mUDP, 'State Ultrasound_Tx '); 
            fprintf(s1UDP, 'State Ultrasound_Tx '); 
            fprintf(s2UDP, 'State Ultrasound_Tx ');
            
            out = 'Sent Ultrasound Commands to Devices';
        end
    end
end