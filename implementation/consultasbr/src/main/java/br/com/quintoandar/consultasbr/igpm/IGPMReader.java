package br.com.quintoandar.consultasbr.igpm;

import br.com.quintoandar.consultasbr.core.IndicePrecoReader;

/**
 * @author <a href="mpereira@quintoandar.com.br">Moacyr</a>
 **/
public class IGPMReader extends IndicePrecoReader {

	public void processar(){
		String javascriptFilename = "igpmf";
		int numMesesOffset = 1;
		super.processar(this.buscar(numMesesOffset, javascriptFilename));
	}

	public static void main(String[] args) {
		IGPMReader r = new IGPMReader();
		r.processar();
		System.out.println(r.getMes());
		System.out.println(r.getAcumulado());
		System.out.println(r.getMensal());
	}
}