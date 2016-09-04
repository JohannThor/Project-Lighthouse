import math
import random


def getDistance(loc1,loc2):
	ydist = math.fabs(loc1[0] - loc2[0]) * (2*math.pi/360) * earthRadius
	xdist = math.fabs(loc1[0] - loc2[0]) * (2*math.pi/360) * earthRadius * math.cos(math.fabs(loc2[0] - loc2[0]));
	return math.sqrt(xdist**2 + ydist**2)


def adjustLatitude(current, delta):
	result = current + (delta / earthRadius) * (180 / math.pi)
	if result > 90:
		return 90 - (result - 90)
	elif result < -90:
		return -90 - (result + 90)
	else:
		return result
	
def adjustLongitude(currentLongitude, currentLatitude, delta):
	result = currentLongitude + (delta / earthRadius) * (180 / math.pi) / math.cos(currentLatitude * math.pi/180);
	if result > 180:
		return -180 + (result - 180)
	elif result < -180:
		return 180 + (result + 180)
	else:
		return result


sign = lambda x: 1 if x > 0 else -1 if x < 0 else 0

# Global specifics
epsilon = 0.001
earthRadius = 6378000

# Agent specifics
rotationalSpeed = 10 # r/sec
velocity = 0.1 # m/sec

# Scenario specifics
obstacles = [{"position":(51.52442309598996,-0.13319266849551986),"id":1}]
initialPosition = (51.52443805519336,-0.13315006083652636)
initialHeading = 250.584628955#70.65423956540306
commands = [{"type":"displacement","value":4},{"type":"displacement","value":-4}] #,{"type":"heading","value":360}
sensors = [{"name":"Distance","range":10,"arc":20,"file":open("Distance","w"),"noise":{"stdev":0.00001}}]

locationFile = open("Location","w")

elapedTime = 0
tick = 0.001
position = initialPosition
heading = initialHeading
currentCommand = commands.pop()
currentVelocity = currentCommand["value"]
cumulativeTick = 0.0
locationNoise = 0.00001

while True:
	cumulativeTick += tick
	# Move
	if currentCommand["type"] == "heading":
		delta = sign(currentCommand["value"]) * rotationalSpeed * tick
		heading = (heading + delta) % 360
		currentCommand["value"] -= delta
	if currentCommand["type"] == "displacement":
		#print position
		delta = sign(currentCommand["value"]) * velocity * tick
		position = ( adjustLongitude(position[0],position[1],delta * math.sin(heading)), adjustLatitude(position[1],delta * math.cos(heading)))
		currentCommand["value"] -= delta
		#print str(position) + str(delta)
	
	# Sense
	for obstacle in obstacles:
		bearing = math.degrees(math.atan2(obstacle["position"][1] - position[1],obstacle["position"][0] - position[0]))
		bearing = (360 + bearing) % 360
		distance =  getDistance(position,obstacle["position"]) #math.sqrt(math.pow(position[0] - obstacle["position"][0],2) + math.pow(position[1] - obstacle["position"][1],2))
		for sensor in sensors:
			distanceWithNoise = random.normalvariate(distance,sensor["noise"]["stdev"])
			print(str(distance) + " : " + str(distanceWithNoise) + " : " + str(sensor["noise"]["stdev"]) + " : " + str(heading) + " : " + str(bearing))
			if distanceWithNoise < sensor["range"] and math.fabs(bearing - heading) < sensor["arc"]/2:
				sensor["file"].write(str(cumulativeTick) + "," + str(distanceWithNoise) + "," + str(currentVelocity) +"\n")
				#print(str(bearing) + " : " + str(math.fabs(bearing-heading)) + " : " + str(math.fabs(bearing - heading) < sensor["arc"]/2))
				#print("#####################################")
				#print("Distance: " + str(distance))
				#print("Bearing: " + str(bearing))
				#print("Type: " + sensor["name"])
				#print("Obstacle: " + str(obstacle["id"]))
				#print("#####################################")
			else:
				sensor["file"].write(str(cumulativeTick) + ",,\n")

	locationFile.write(str(cumulativeTick) + "," + str(random.normalvariate(position[0],locationNoise)) + "," + str(random.normalvariate(position[1],locationNoise)) + "," + str(heading) + "\n")

	#print("HEADING: " + str(heading) + "  --- POSITION: " + str(position))
	
	if math.fabs(currentCommand["value"]) < epsilon:
		if len(commands) == 0:
			break
		else:
			currentCommand = commands.pop()
			currentVelocity = currentCommand["value"]























