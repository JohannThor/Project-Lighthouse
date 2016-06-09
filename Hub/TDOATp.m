function [coordinates] = TDOATp(N,nodesPos,rx)
%PARAMETERS:
%   N: Number of nodes in the room
%   nodesPos: The position of the nodes in cartesian coordinates stored in an N x 2 matrix of format
%       X1 X2 ..... XN
%       Y1 Y2 ..... YN
%       Order should follow from nodes transmitting the lowest frequency to the highest frequency
%   rx: The array of received ultrasound amplitude data points
%RETURNS: 
%   coordinates: Could be the following depending on the following conditions: 
%       For 0 correlation: returns 0 (Meaning no correlation possible,
%           could mean receiver not in same location as the same nodes
%           specified by their positions nodesPos)
%       For 1 correlation: returns 1 (Meaning detected by a node in the room)
%       For 2 correlations: returns either 1x2 matrix or 2x2 matrix with
%           possible coordinates relative to the provided nodesPos
%       For 3 correlations: returns 1x2 matrix specifying the X-Y
%           coordinates relative to the provided nodesPos
%%%%%%%%%%%%%%%%%%%%%%%%%% SYSTEM PARAMETERS %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
fmin = 31800;
fmax = 33800;
%The minimum and maximum frequencies used in the room (Note program assumes
%linear frequency assignment) 
Fs = 125*(10^3);
%Sampling frequency of the band
noOfCycles = 1600; %Chosen based on optimising the cross correlation
%Number of periods being transmitted
speedOfSound = 330;
%Speed of sound in m/s
%%%%%%%%%%%%%%%%%%%%%%% SIMULATED TRANSMITTED SIGNAL %%%%%%%%%%%%%%%%%%%%%%
tx = cell(N,1);
freq = fmin;
for i = 1:N
    tx_t = 0:(1/Fs):(noOfCycles*(1/freq));
    tx{i,1} = sin(2*pi*(freq)*tx_t);
    freq = freq + ((fmax-fmin)/(N-1));
end
%The transmitted signal 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PERFORMS CROSS CORRELATION %%%%%%%%%%%%%%%%%%
distAndCL = zeros(2,N);
%Array of distance-correlation level pairs
maxDistAndCL = 0;
%Stores the maximum distance-correlation pair
maxIndex = 0;
%Stores the index in distAndCL that maxDistAndCL is in
for i = 1:N
    try
        [r,lag] = xcorr(rx,tx{i,1});
        %Perform the spectral based cross correlation
        
        [up,~]=envelope(r);
        %Gets the envelope of the cross correlation

        [A,I] = max(up);
        I2 = lag(I);
        %Obtain the maximum correlation peak from the envelope
        
        %In the following code, the threshold for eliminating the presense
        %of a signal is 1/4 of the maximum CL detected
        %(Used to eliminate the influence of cross correlation between two
        %frequencies when one of the two frequencies isnt presence but
        %appears due to spectral overlap
        
        if(i>1)
            if(maxDistAndCL(2)<(A/4))
                distAndCL(:,maxIndex) = [0;0];
                %Set the previous maximum as zero as weak compared to this
                %one
                distAndCL(:,i) = [abs((I2/Fs)*speedOfSound) ; A];
                %Add the distance-correlation level of this one into the
                %array
                maxDistAndCL = distAndCL(:,i);
                %Replace the previous maximum distance-correlation level
                %with this one
                maxIndex = i;
                %Replace the previous index with this one
            elseif(maxDistAndCL(2)>(4*A))
                distAndCL(:,i) = [0;0];
                %Set the current index in the distance-correlation as zero
                %as this one is weakly correlated
            else
               distAndCL(:,i) = [abs((I2/Fs)*speedOfSound) ; A];
               %In all other cases, set this distance-correlation in the
               %array
               %(This happens for values of CL that are neither weakly
               %correlated or strongly correlated)
               if(maxDistAndCL(2)<A)
                    maxDistAndCL = distAndCL(:,i);
                    maxIndex = i;
                    %If the current CL is greater or equal to than the maximum, set
                    %this one as max
               end
            end
        else
            distAndCL(:,i) = [abs((I2/Fs)*speedOfSound) ; A];
            %Adds the first point to the distance-correlation level array
            maxDistAndCL = distAndCL(:,i);
            maxIndex = i;
            %Sets the maximum distance-correlation point as this one as it
            %is the first point
        end
    catch
        distAndCL(:,i) = [0;0];
        %Handles the case where no correlation is avaliable
    end
end
disp(distAndCL)
%%%%%%%%%%%%%%%%%%%%%%%%%%PROCESS DIST-CL FOR RELIABLE DIST (ONLY USEFUL WHEN MORE THAN 3 NODES)%%%%%%%%%%%%%%%%
[~,I]=sort(distAndCL(2,:),'descend');
%Sorts based on level of correlation
dist = zeros(1,N);
nodesXPos = zeros(1,N);
nodesYPos = zeros(1,N);
%Stores the distance and coordinates
for i = 1:3
    %Gets the top 3 correlated distance
    B = nodesPos(:,I(i));
    nodesXPos(i) = B(1);
    nodesYPos(i) = B(2);
    %Gets the corresponding 2x1 matrix & Separates the X coordinates and Y coordinates
    B = distAndCL(:,I(i));
    dist(i) = B(1);
    %Gets the corresponding 2x1 matrix & extract the distance measurements
end
%%%%%%%%%%%%%%%%%%%%%%%%%%CALCULATE TRIANGULATION POS%%%%%%%%%%%%%%%%%%%%%%
indices = find(dist>0);
countOfNonZeroDist = length(indices);
%Finds the index of the non-zero distances
if(countOfNonZeroDist==1)
    coordinates = 1;
elseif(countOfNonZeroDist==2)
    [xout3 yout3] = circcirc(nodesXPos(indices(1)),nodesYPos(indices(1)),abs(dist(indices(1))),nodesXPos(indices(2)),nodesYPos(indices(2)),abs(dist(indices(2))));
    coordinates = [xout3;yout3];
    %Output 2 coordinates at most
elseif(countOfNonZeroDist==3)
    %CASE: IF top 3 correlated distance are non-zero
    [xout3 yout3] = circcirc(nodesXPos(indices(1)),nodesYPos(indices(1)),dist(indices(1)),nodesXPos(indices(2)),nodesYPos(indices(2)),dist(indices(2)));
    [xout4 yout4] = circcirc(nodesXPos(indices(1)),nodesPos(indices(1)),dist(indices(1)),nodesXPos(indices(3)),nodesYPos(indices(3)),dist(indices(3)));
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    coord1 = [xout3;yout3];
    coord2 = [xout4;yout4];
    %Creates an matrix
    err = abs(coord1 - coord2);
    [~,index]=min(err,[],2);
    %Finds the minimum error between the matrices i.e. the closest intersecting
    %coordinates between the multiple circles
    closestCoordinates = (coord1(:,index(1,1)) + coord2(:,index(1,1)))/2;
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%one method of approximation%%%%%%%%%%%%%%%%
    coordinates = [closestCoordinates(1,1);closestCoordinates(2,1)];
else
    coordinates = 0;
    %No correlation possible
end

end
