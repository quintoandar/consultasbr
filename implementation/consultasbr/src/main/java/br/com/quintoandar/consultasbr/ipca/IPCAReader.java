package br.com.quintoandar.consultasbr.ipca;

import br.com.quintoandar.consultasbr.core.IndicePrecoReader;

public class IPCAReader extends IndicePrecoReader {

    public void processar(){
        String javascriptFilename = "ipca";
        int numMesesOffset = 2;
        super.processar(this.buscar(numMesesOffset, javascriptFilename));
    }

    public static void main(String[] args) {
        IPCAReader r = new IPCAReader();
        r.processar();
        System.out.println(r.getMes());
        System.out.println(r.getAcumulado());
        System.out.println(r.getMensal());
    }
}