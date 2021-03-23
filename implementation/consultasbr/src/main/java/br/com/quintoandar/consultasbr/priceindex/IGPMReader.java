package br.com.quintoandar.consultasbr.priceindex;

/**
 * @author <a href="mpereira@quintoandar.com.br">Moacyr</a>
 **/
public class IGPMReader extends IndicePrecoReader {

	@Override
	protected String getJavascriptFilename() {
		return "igpmf";
	}

	@Override
	protected int getNumMesesOffset() {
		return 1;
	}

	public static void main(String[] args) {
		IGPMReader r = new IGPMReader();
		r.crawl();
		System.out.println(r.getMes());
		System.out.println(r.getAcumulado());
		System.out.println(r.getMensal());
	}
}