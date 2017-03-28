#!/usr/bin/python
# Open a file
file = open("backend.txt", "wb")
for i in range(1000):
	file.write( '{"fecha":"'+str(i*10000)+'", "productos":[{"producto": {"id":'+str(11)+'}, "cantidad": '+str(5)+'},{"producto": {"id":'+str(8)+'}, "cantidad": '+str(2)+'}], "proveedor": {"id":'+str(4)+'}}\n');

file.close()


