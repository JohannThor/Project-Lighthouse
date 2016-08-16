import math
import random

sign = lambda x: 1 if x > 0 else -1 if x < 0 else 0

# Global specifics
epsilon = 0.001

# Agent specifics
rotationalSpeed = 10 # r/sec
velocity = 1 # m/sec

# Scenario specifics
obstacles = [{"position":(6,0),"id":2}]#{"position":(2,2),"id":1},
initialPosition = (0,0)
initialHeading = 0
commands = [{"type":"displacement","value":-4},{"type":"displacement","value":4}]
sensors = [{"name":"Latitude","range":10,"arc":270,"file":open("Latitude","w"),"noise":{"stdev":0.1}},{"name":"Longitude","range":10,"arc":270,"file":open("Longitude","w"),"noise":{"stdev":0.2}},{"name":"Confidence","range":10,"arc":270,"file":open("Confidence","w"),"noise":{"stdev":0.3}}]


elapedTime = 0
tick = 0.01
position = initialPosition
heading = initialHeading
currentCommand = commands.pop()
currentVelocity = currentCommand["value"]
cumulativeTick = 0.0

while True:
	cumulativeTick += tick
	# Move
	if currentCommand["type"] == "heading":
		delta = sign(currentCommand["value"]) * rotationalSpeed * tick
		heading = (heading + delta) % 360
		currentCommand["value"] -= delta
	if currentCommand["type"] == "displacement":
		delta = sign(currentCommand["value"]) * velocity * tick
		position = (position[0] + math.cos(heading)*delta, position[1] + math.sin(heading)*delta)
		currentCommand["value"] -= delta
	
	# Sense
	for obstacle in obstacles:
		bearing = math.atan2(position[0] - obstacle["position"][0], position[1] - obstacle["position"][1])
		distance = math.sqrt(math.pow(position[0] - obstacle["position"][0],2) + math.pow(position[1] - obstacle["position"][1],2))
		for sensor in sensors:
			distanceWithNoise = random.normalvariate(distance,sensor["noise"]["stdev"])
			print(str(distance) + " : " + str(distanceWithNoise) + " : " + str(sensor["noise"]["stdev"]))
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

	
	#print("HEADING: " + str(heading) + "  --- POSITION: " + str(position))
	
	if math.fabs(currentCommand["value"]) < epsilon:
		if len(commands) == 0:
			break
		else:
			currentCommand = commands.pop()
			currentVelocity = currentCommand["value"]























