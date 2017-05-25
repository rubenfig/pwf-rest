package py.pol.una.ii.pw.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@XmlRootElement
@Table(name = "Venta")
@NamedQueries( {
		@NamedQuery( name = "Venta.listAll", query = "SELECT u FROM Venta u" ),
		@NamedQuery( name = "Venta.queryRecordsSize", query = "SELECT count(u) FROM Venta u" )})
public class Venta implements Serializable {
    /** Default value included to remove warning. Remove or modify at will. **/
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Column(name = "fecha")
    private String fecha;
    
//    @NotNull
//    @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
//    @JoinTable(name = "ventas_productos", joinColumns = @JoinColumn(name = "id_Venta"), inverseJoinColumns = @JoinColumn(name = "id_Producto"))
//    private List<Producto> productos;
    
    @NotNull
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "ventas_productos", joinColumns = @JoinColumn(name = "id_Venta"), inverseJoinColumns = @JoinColumn(name = "id_ProductoComprado"))
    private List<ProductoComprado> productos;
    
    @NotNull
    @ManyToOne
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public List<ProductoComprado> getProductos() {
		return productos;
	}

	public void setProductos(List<ProductoComprado> productos) {
		this.productos = productos;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
    	
}
