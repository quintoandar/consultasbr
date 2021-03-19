package br.com.quintoandar.consultasbr.core;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class IndicePrecoReader {
    private static final String HTTP_PORTALDEFINANCAS_COM = "http://portaldefinancas.com/";
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy",new Locale("pt", "BR"));

    private BigDecimal mensal;
    private BigDecimal acumulado;
    private Date mes;

    public IndicePrecoReader() {
    }
    public String buscar(int numMesesOffset, String javascriptFilename) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.MONTH, -numMesesOffset);
            int year = c.get(Calendar.YEAR);

            String scriptSrc = HTTP_PORTALDEFINANCAS_COM + "/js-inf/"+ javascriptFilename + "-" + year + ".js";

            URL url = new URL(scriptSrc);
            URLConnection con = url.openConnection();
            InputStream in = con.getInputStream();
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void processar(String retorno) {
        try {
            Pattern p = Pattern.compile("document\\.write\\('(.+)'\\)");
            Matcher mat = p.matcher(retorno);

            if(mat.find()){
                Document doc = Jsoup.parse("<html><body><table>"+mat.group(1)+"</table></body></html>");
                Elements trs = doc.select("tr");
                for(Element tr:trs){
                    Elements tds = tr.select("td");
                    this.mes = sdf.parse("01/"+tds.get(0).html());
                    this.mensal = new BigDecimal(tds.get(1).html().replaceAll(",", "."));
                    this.acumulado = new BigDecimal(tds.get(3).html().replaceAll(",", "."));
                    break;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public BigDecimal getMensal() {
        return mensal;
    }

    public BigDecimal getAcumulado() {
        return acumulado;
    }

    public Date getMes() {
        return mes;
    }
}
