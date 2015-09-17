package br.com.quintoandar.consultasbr.crecisp;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.quintoandar.consultasbr.core.SimpleHttpQuerier;

public class ConsultarCreci extends SimpleHttpQuerier {
	private static final String ENCODING = "ISO-8859-1";

	private static final Logger log = LoggerFactory.getLogger(ConsultarCreci.class);

	public static SimpleDateFormat sdfRecebido = new SimpleDateFormat("dd/MM/yyyy");
	
	public ResultadoCreci consultar(String creci, TipoCreci tipo) {
		Map<String, String> map = new HashMap<String, String>();

		map.put("acao", "emitir");
		map.put("creci", creci.replaceAll("[^0-9]", ""));
		map.put("pessoa", tipo.getHtmlId());
		
		String searchUrl = "http://www.crecisp.gov.br/certidao/certidao.asp";
		HttpPost httpGet = new HttpPost(searchUrl);
		httpGet.addHeader("Accept", "text/html");
		httpGet.addHeader("Accept-Encoding", "identity");
		httpGet.addHeader("Accept-Language", "en-US,en;q=0.8");
		httpGet.addHeader("Connection", "keep-alive");
		httpGet.addHeader("Host", "www.crecisp.gov.br");
		httpGet.addHeader("Origin", "http://www.crecisp.gov.br");
		httpGet.addHeader("Referer", "http://www.crecisp.gov.br/certidao/");
		httpGet.setEntity(getMapEntity(ENCODING, map, null));

		ResultadoCreci resCons = new ResultadoCreci();
		resCons.setUrlBusca(searchUrl);
		resCons.setCreci(creci);
		resCons.setTipo(tipo);

		try {
			HttpResponse resp = client.execute(httpGet);
			if (resp.getStatusLine().getStatusCode() == 200) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				String html = new String(baos.toByteArray(), ENCODING);
				baos.close();
				baos = null;

				resCons.setHtml(getHtmlWithBase(html, ENCODING,"http://www.crecisp.gov.br/certidao/"));

				Document doc = Jsoup.parse(html.replaceAll("&nbsp;", " "));
				Elements els = doc.select("p.p1");
				for (Element e : els) {
					final String txt = e.text();
					if(txt.contains("CERTIFICAMOS") && txt.contains("“ATIVO”")){
						resCons.setStatus(ResultadoCreci.Status.Ativo);
						break;
					}
				}
			}
		} catch (Throwable e) {
			log.error("Erro", e);
			throw new RuntimeException("Erro inesperado",e);
		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		return resCons;
	}
}
