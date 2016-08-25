import math




def getDistance(loc1,loc2):
	ydist = math.fabs(loc1[0] - loc2[0]) * (2*math.pi/360) * earthRadius
	
	xdist = math.fabs(loc1[0] - loc2[0]) * (2*math.pi/360) * earthRadius * math.cos(math.fabs(loc2[0] - loc2[0]));

	return math.sqrt(xdist**2 + ydist**2)

earthRadius = 6378000

obstacle = (51.52442309598996,-0.13319266849551986)
initialPosition = (51.52443805519336,-0.13315006083652636)


print getDistance(initialPosition,obstacle)

