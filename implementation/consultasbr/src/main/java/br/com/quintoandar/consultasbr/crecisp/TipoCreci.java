package br.com.quintoandar.consultasbr.crecisp;

public enum TipoCreci {
	Fisica, Juridica;

	private String htmlId;

	private TipoCreci() {
		this(null);
	}

	private TipoCreci(String htmlId) {
		this.htmlId = htmlId;
	}

	public String getHtmlId() {
		if (htmlId == null) {
			htmlId = name().substring(0, 1).toUpperCase();
		}
		return htmlId;
	}
}