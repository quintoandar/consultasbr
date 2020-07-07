package br.com.quintoandar.consultasbr.igpm;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.com.quintoandar.consultasbr.core.SimpleHttpQuerier;

/**
 * @author <a href="mpereira@quintoandar.com.br">Moacyr</a>
 **/
public class IGPMReader extends SimpleHttpQuerier {

	private static final String HTTP_PORTALDEFINANCAS_COM = "http://portaldefinancas.com/";

	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy",new Locale("pt", "BR"));

	public BigDecimal mensal;

	public BigDecimal acumulado;

	public Date mes;

	String buscar() {
		try {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.MONTH, -1);
			int year = c.get(Calendar.YEAR);

			String scriptSrc = HTTP_PORTALDEFINANCAS_COM + "/js-inf/igpmf-" + year + ".js";

			URL url = new URL(scriptSrc);
			URLConnection con = url.openConnection();
			InputStream in = con.getInputStream();
			return IOUtils.toString(in, "UTF-8");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	void processarIgpm(String retorno) {
		Pattern p = Pattern.compile("document\\.write\\('(.+)'\\)");
		Matcher mat = p.matcher(retorno);
		if(mat.find()){
			Document doc = Jsoup.parse("<html><body><table>"+mat.group(1)+"</table></body></html>");
			Elements trs = doc.select("tr");
			for(Element tr:trs){
				Elements tds = tr.select("td");
				try {
					this.mes = sdf.parse("01/"+tds.get(0).html());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				this.mensal = new BigDecimal(tds.get(1).html().replaceAll(",", "."));
				this.acumulado = new BigDecimal(tds.get(3).html().replaceAll(",", "."));
				break;
			}
		}
	}
	
	public void processar(){
		processarIgpm(buscar());
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
	
	

	public static void main(String[] args) {
		IGPMReader r = new IGPMReader();
		r.processar();
		System.out.println(r.getMes());
		System.out.println(r.getAcumulado());
		System.out.println(r.getMensal());
	}
}