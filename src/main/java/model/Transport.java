package main.java.model;

public enum Transport {
	NONE(0,"None",""),
	TAXI(1,"TaxiTicket","T"),
	BUS(2,"BusTicket","B"),
	UG(3,"UndergroundTicket","U"),
	FERRY(50,"Black","F"),
	BLACK(60,"black","");

	private int id;
	private String name, abbreviation;

	Transport(int id, String name, String abbreviation) {
		this.id = id;
		this.name = name;
		this.abbreviation = abbreviation;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static Transport findById(int id){
		for (Transport transport : values()) {
			if(transport.id == id){
				return transport;
			}
		}
		return null;
	}

	public static Transport findByName(String name){
		for (Transport transport : values()) {
			if(transport.name.equals(name)){
				return transport;
			}
		}
		return null;
	}

	public static Transport findByAbbreviation(String abbr){
		for (Transport transport : values()) {
			if(transport.abbreviation.equals(abbr)){
				return transport;
			}
		}
		return null;
	}
}
