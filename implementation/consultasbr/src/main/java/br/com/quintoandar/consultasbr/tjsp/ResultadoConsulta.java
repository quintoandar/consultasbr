package br.com.quintoandar.consultasbr.tjsp;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class ResultadoConsulta implements Serializable {

	private static final long serialVersionUID = -4585362220236747177L;

	private byte[] pdf;

	private String urlBusca;

	private int pagina;

	private List<ResultadosTJSP> resultados = new LinkedList<ResultadosTJSP>();

	public byte[] getPdf() {
		return pdf;
	}

	public void setPdf(byte[] pdf) {
		this.pdf = pdf;
	}

	public String getUrlBusca() {
		return urlBusca;
	}

	public void setUrlBusca(String urlBusca) {
		this.urlBusca = urlBusca;
	}

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

	public List<ResultadosTJSP> getResultados() {
		return resultados;
	}

	public void setResultados(List<ResultadosTJSP> resultados) {
		this.resultados = resultados;
	}

	public void add(ResultadosTJSP res) {
		resultados.add(res);
	}

}
