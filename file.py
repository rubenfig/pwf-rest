#!/usr/bin/python
# Open a file
file = open("backend_ventas.txt", "wb")
for i in range(1000):
	file.write( '{"fecha":"'+str(i*10000)+'", "productos":[{"producto": {"id":'+str(11)+'}, "cantidad": '+str(5)+'},{"producto": {"id":'+str(8)+'}, "cantidad": '+str(2)+'}], "cliente": {"id":'+str(6)+'}}\n');

file.close()


