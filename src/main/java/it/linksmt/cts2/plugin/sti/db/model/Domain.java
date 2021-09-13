package it.linksmt.cts2.plugin.sti.db.model;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "domain")
public class Domain implements java.io.Serializable {

	private Long id;
	private String key;
	private String name;
	private Integer position;
	private Integer state;
	
	
	public Domain() {
	}
	

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	@Column(name = "key", nullable = false, length = 65535)
	public String getKey() {
		return this.key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	@Column(name = "name", length = 65535)
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Column(name = "position", nullable = true)
	public Integer getPosition() {
		return this.position;
	}

	public void setPosition(final Integer position) {
		this.position = position;
	}

	@Column(name = "state", nullable = true)
	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

}
