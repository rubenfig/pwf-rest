import json
import os
from collections import OrderedDict
import random
import psycopg2

conn = psycopg2.connect("dbname='pwf-backend' user='postgres' host='localhost' password='postgres'")
cur = conn.cursor()
os.system('clear')
n = int(raw_input("Ingrese la cantidad de productos a genera: "))


for i in range(0, n):
    producto = OrderedDict()
    name = "Producto "+str(i+1)
    descripcion = "Descripcion "+str(i+1)
    # print name
    cur.execute("""INSERT INTO producto (id,nombre,descripcion,precio, id_proveedor) VALUES (%s, %s, %s, 2)""",( i+1, name,descripcion,random.randint(1000,10000)))
    conn.commit()

    


