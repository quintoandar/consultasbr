package br.com.quintoandar.consultasbr.igpm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
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

	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/34.0.1847.116 Chrome/34.0.1847.116 Safari/537.36";
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yyyy",new Locale("pt", "BR"));

	public BigDecimal mensal;

	public BigDecimal acumulado;

	public Date mes;

	String buscar() {
		// this.P
		// buscar indice pela url
		HttpGet httpGet = new HttpGet(HTTP_PORTALDEFINANCAS_COM+"igp_m_fgv.htm");
		httpGet.setHeader("User-Agent", USER_AGENT);
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("Accept-Language", "en-US,en;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");

		try {
			HttpResponse resp = client.execute(httpGet);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			resp.getEntity().writeTo(baos);
			String retorno = new String(baos.toByteArray(), "ISO-8859-1");
			Document doc = Jsoup.parse(retorno);
			Elements media = doc.select("script[src*=js-inf/igp-m-fgv]");
			Element ultimoIgpm = media.get(0);

			String scriptSrc = ultimoIgpm.attr("src").trim();
			if(scriptSrc.matches("^/.*") || !scriptSrc.matches("^(www|http://).*")){
				scriptSrc = HTTP_PORTALDEFINANCAS_COM.replaceAll("/+$","")+"/"+scriptSrc.replaceAll("^/+","");
			}
			
			httpGet = new HttpGet(scriptSrc);
			httpGet.setHeader("User-Agent", USER_AGENT);
			httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpGet.setHeader("Accept-Language", "en-US,en;q=0.8");
			httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");

			resp = client.execute(httpGet);
			baos = new ByteArrayOutputStream();
			resp.getEntity().writeTo(baos);
			retorno = new String(baos.toByteArray(), "ISO-8859-1");

//			'forcing' gc
			ultimoIgpm = null;
			media = null;
			doc = null;
			baos = null;
			
//			processar(retorno);
			return retorno;
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
//		retorno = retorno.replaceAll(regex, replacement)
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