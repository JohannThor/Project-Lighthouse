import math

sign = lambda x: 1 if x > 0 else -1 if x < 0 else 0

# Global specifics
epsilon = 0.001

# Agent specifics
rotationalSpeed = 10 # r/sec
velocity = 1 # m/sec

# Scenario specifics
obstacles = [{"position":(3,3),"id":2}]#{"position":(2,2),"id":1},
initialPosition = (0,0)
initialHeading = 0
commands = [{"type":"heading","value":360},{"type":"displacement","value":2},{"type":"heading","value":360}]
sensors = [{"name":"Ultrasound","range":5,"arc":10,"noise":{"stdev":0.1,"amplitude":1}},{"name":"Infrared","range":4,"arc":10,"noise":{"stdev":0.1,"amplitude":1}}]


elapedTime = 0
tick = 0.01
position = initialPosition
heading = initialHeading
currentCommand = commands.pop()

while True:
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
			if distance < sensor["range"] and math.fabs(bearing - heading) < sensor["arc"]/2:
				#print(str(bearing) + " : " + str(math.fabs(bearing-heading)) + " : " + str(math.fabs(bearing - heading) < sensor["arc"]/2))
				print("#####################################")
				print("Distance: " + str(distance))
				print("Bearing: " + str(bearing))
				print("Type: " + sensor["name"])
				print("Obstacle: " + str(obstacle["id"]))
				print("#####################################")
	
	print("HEADING: " + str(heading) + "  --- POSITION: " + str(position))
	if math.fabs(currentCommand["value"]) < epsilon:
		if len(commands) == 0:
			break
		else:
			currentCommand = commands.pop()























