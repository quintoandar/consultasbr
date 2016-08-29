package br.com.quintoandar.consultasbr.iptusp;

import java.util.ArrayList;
import java.util.List;

public class RespostaConsulta2ViaIptu {
	
	private List<Resposta2ViaIPTU> list2viaIptus = new ArrayList<Resposta2ViaIPTU>();
	
	private List<IPTUSPException> exceptions = new ArrayList<IPTUSPException>();

	public List<Resposta2ViaIPTU> getList2viaIptus() {
		return list2viaIptus;
	}

	public void setList2viaIptus(List<Resposta2ViaIPTU> list2viaIptus) {
		this.list2viaIptus = list2viaIptus;
	}
	
	public List<IPTUSPException> getExceptions(){
		return this.exceptions;
	}
	
	public void addException(IPTUSPException e){
		exceptions.add(e);
	}

}
