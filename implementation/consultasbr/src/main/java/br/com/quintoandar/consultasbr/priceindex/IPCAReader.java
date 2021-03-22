package br.com.quintoandar.consultasbr.priceindex;

public class IPCAReader extends PriceIndexReader {

    @Override
    protected String getJavascriptFilename() {
        return "ipca";
    }

    @Override
    protected int getNumMesesOffset() {
        return 2;
    }

    public static void main(String[] args) {
        IPCAReader r = new IPCAReader();
        r.crawl();
        System.out.println(r.getMes());
        System.out.println(r.getAcumulado());
        System.out.println(r.getMensal());
    }
}