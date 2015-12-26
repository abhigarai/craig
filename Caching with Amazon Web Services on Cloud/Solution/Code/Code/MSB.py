import vertx
import urllib2
import sys
import socket

server = vertx.create_http_server()
database_instances = ["" for x in range(2)]

#Initialize the variables that store the DNS of database instances
def init():
	database_instances[0]="<INSERT FIRST DCI'S DNS HERE>"
	database_instances[1]="<INSERT SECOND DCI'S DNS HERE>"

#Send HTTP request
def send_request(host,url):
	Port=80 #Port for HTTP connection
	
	try:
		sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	except socket.error, msg:
		sys.stderr.write("ERROR: %s\n" % msg[1])
		sys.exit(1)
 
	try:
		sock.connect((host, Port))
	except socket.error, msg:
		sys.stderr.write("ERROR %s\n" % msg[1])
		sys.exit(1)
 
	sock.send("GET %s HTTP/1.0\r\nHost: %s\r\n\r\n" % (url, host))
 
	data = sock.recv(1024)
	string = ""
	while len(data):
		string = string + data
		data = sock.recv(1024)
	sock.close()

	return string
	
#Send a request to the backend datacenter
def send_request_to_datacenter(host, url):
	response_from_server = send_request(host,url)
	result = ""
	parsed_response = response_from_server.split()[7:]

	for r in parsed_response:
		result += r + " "

	return result.strip()
		
#Check that the DCI are up and running
def check_backend():
	DC0_response = send_request(database_instances[0], generate_path(1))
	DC1_response = send_request(database_instances[1], generate_path(1))
	if (DC0_response == "" or DC1_response == "" or 
	DC0_response.split()[1] != '200' or DC1_response.split()[1] != '200'):
		return 0
	else:
		return 1
		
#Generate the path for a single targetID
def generate_path(targetID):
	return "/target?targetID="+str(targetID)


#Generate the path for a range of targetIDs
#The details of 10,000 most recently targets are cached in the database instance
def generate_range_path(start_range, end_range):
	return "/range?start_range=" + start_range + "&end_range="+end_range

#Takes targetID as the input and returns the record for that targetID
#You need to optimize this function
def retrieve_details(targetID):
	return send_request_to_datacenter(database_instances[0], generate_path(targetID))

#Calls the retrieve details function
def process_request(targetID, req):
	result = retrieve_details(targetID)
	if result == "":
		req.response.end("No response received")
	else:
		req.response.end(result)

init()

#Check that the backend instances are up
if(check_backend() == 0):
        print "ERROR: Unable to connect to data center instances"
	sys.exit()

#Handle the request
@server.request_handler
def handle(req):
	if req.path == "/target":
		targetID=req.params['targetID']
		process_request(targetID, req)
	if req.path == "/":
		targetID=1
		process_request(targetID, req)
server.listen(80)
