package br.com.quintoandar.consultasbr.iptusp;


import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.quintoandar.consultasbr.core.SimpleHttpQuerier;

public class ConsultarIptuSP extends SimpleHttpQuerier {
	private static final String ENCODING = "ISO-8859-1";

	private static final Logger log = LoggerFactory.getLogger(ConsultarIptuSP.class);

	public static SimpleDateFormat sdfRecebido = new SimpleDateFormat("dd/MM/yyyy");

	public Resposta2ViaIPTU buscar2aViaIPTU(String codContribuinte, Integer parcela, Integer anoExercicio) {
		Map<String, String> map = new HashMap<String, String>();
		codContribuinte = codContribuinte.replaceAll("[^0-9]", "");
		map.put("txt_contribuinte1", codContribuinte.substring(0,3));
		map.put("txt_contribuinte2", codContribuinte.substring(3,6));
		map.put("txt_contribuinte3", codContribuinte.substring(6,10));
		map.put("txt_contribuinte4", codContribuinte.substring(10,11));
		map.put("cmb_parcela", (parcela < 10 ? "0" : "") + parcela.toString());
		map.put("txt_exercicio", anoExercicio.toString());

		HttpPost httpPost = new HttpPost("http://www3.prefeitura.sp.gov.br/iptusimp/index.jsp");
		httpPost.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httpPost.setEntity(getMapEntity(ENCODING, map, null));

		client.getCookieStore().clear();
		try {
			HttpResponse resp = client.execute(httpPost);
			if (resp.getStatusLine().getStatusCode() == 200) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				String contentAsString = new String(baos.toByteArray(), ENCODING).replaceAll("[\n\r]", "");
				baos.close();
				baos = null;

				Document doc = Jsoup.parse(contentAsString.replaceAll("&nbsp;", " "));
				
				Elements fonts = doc.select("table > tbody > tr > td > font");
				log.info("fonts size: "+fonts.size());
				
				Pattern p = Pattern.compile("([0-9]{12}) *([0-9]{12}) *([0-9]{12}) *([0-9]{12})");
				Matcher m;
				Date dataValidade = null;
				String codigo = null;
				for(Element el : fonts){
					if(dataValidade == null && el.ownText().trim().equalsIgnoreCase("data de validade")){
						String dataValidadeText = el.parent().parent().parent().text();
						log.info("Text: "+dataValidadeText);
						dataValidade  = sdfRecebido.parse(dataValidadeText.replaceAll("[^0-9/]", ""));
					} else if(codigo == null && (m = p.matcher(el.text())).find()){
						codigo = m.group(1)+m.group(2)+m.group(3)+m.group(4);
						log.info("Cod: "+codigo);
					}
					if(dataValidade != null && codigo != null){
						break;
					}
				}
				
				baos = new ByteArrayOutputStream();
				baos.write(getHtmlWithBase(contentAsString, ENCODING,"http://www3.prefeitura.sp.gov.br/iptusimp/"));
				baos.close();
				
				Resposta2ViaIPTU res = new Resposta2ViaIPTU(codContribuinte,anoExercicio,parcela);
				res.setVencimento(dataValidade);
				res.setCodigo(codigo);
				res.setDado(baos.toByteArray());
				return res;
			}
		} catch (Throwable e) {
			log.error("Erro", e);
		} finally {
			httpPost.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		return null;
	}
	
	public static void main(String[] args) {
		ConsultarIptuSP consultarIptuSP = new ConsultarIptuSP();
		consultarIptuSP.buscar2aViaIPTU("010.078.0045-3", 5, 2014);
	}
}