package br.com.quintoandar.consultasbr.pf;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.quintoandar.consultasbr.core.SimpleHttpQuerier;

public class ConsultarPF extends SimpleHttpQuerier {
	private static final String ENCODING = "ISO-8859-1";

	private static final String JSESSIONID_COOKIE_KEY = "JSESSIONID";

	private static final Logger log = LoggerFactory.getLogger(ConsultarPF.class);

	public static SimpleDateFormat sdfRecebido = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public boolean shouldPersistCookie(BasicClientCookie ck) {
		if(ck.getName().equals(JSESSIONID_COOKIE_KEY)){
			return true;
		}
		return super.shouldPersistCookie(ck);
	}

	public RespostaCaptcha requestCaptcha() {
		HttpGet httpGet = new HttpGet("https://servicos.dpf.gov.br/sinic-certidao/jcaptcha");
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0");

		client.getCookieStore().clear();
		try {
			HttpResponse resp = client.execute(httpGet);
			RespostaCaptcha captchaAnswer = new RespostaCaptcha(getCookieValue(JSESSIONID_COOKIE_KEY));
			if (resp.getStatusLine().getStatusCode() == 200 && resp.getEntity().getContentType().getValue().equalsIgnoreCase("image/jpeg")) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				captchaAnswer = new RespostaCaptcha(getCookieValue(JSESSIONID_COOKIE_KEY),baos.toByteArray());
			}
			return captchaAnswer;
		} catch (Throwable e) {
			log.error("Erro", e);
		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);

		}
		return null;
	}

	private String resolverCaptcha(String sessionId, String captcha, String nome) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("cdImagem",captcha);
		map.put("TI","Validar");
		map.put("nome",nome);
		
		HttpPost httpPost = new HttpPost("https://servicos.dpf.gov.br/sinic-certidao/validacaptcha");
		httpPost.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0");
		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		httpPost.setEntity(getMapEntity(ENCODING, map, null));

		client.getCookieStore().clear();
		if (sessionId != null) {
			novoCookie(JSESSIONID_COOKIE_KEY, sessionId);
			attachCookiesFromStore(httpPost);
		}
		try {
			HttpResponse resp = client.execute(httpPost);
			if (resp.getStatusLine().getStatusCode() == 200) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				String contentAsString = new String(baos.toByteArray(),ENCODING).replaceAll("[\n\r]","");
				baos.close();
				baos = null;
				
				String cod = contentAsString.replaceAll("^.*value='([0-9A-Za-z]*)';.*$", "$1");
				contentAsString = null;
				
				return cod;
			}
			return getCookieValue(resp.getAllHeaders(), JSESSIONID_COOKIE_KEY);
		} catch (Throwable e) {
			log.error("Erro", e);
		} finally {
			httpPost.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		return null;
	}
	
	public  ResultadoAntecedentes consultarAntecedentes(ConsultaAntecedentes consulta) {
		if(consulta.getSessionId() == null){
			throw new IllegalArgumentException("SessionId must be specified");
		}
		if(consulta.getRespostaCaptcha() == null){
			throw new IllegalArgumentException("RespostaCaptcha must be specified");
		}
		String cod = resolverCaptcha(consulta.getSessionId(), consulta.getRespostaCaptcha(), consulta.getNome());
		return consultarAntecedentes(consulta.getSessionId(), cod, consulta.getNome(), consulta.getCpf());
	}

	private ResultadoAntecedentes consultarAntecedentes(String sessionId, String codId, String nome, String cpf) {
		HttpPost httpPost = new HttpPost("https://servicos.dpf.gov.br/sinic-certidao/controle.jsp");
//		HttpPost httpPost = new HttpPost("http://localhost:10001/sinic-certidao/controle.jsp");
		httpPost.addHeader("Host","servicos.dpf.gov.br");
		httpPost.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0");
		httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		httpPost.addHeader("Accept-Language", "en-US,en;q=0.5");
		httpPost.addHeader("Accept-Encoding", "deflate");
		httpPost.addHeader("Referer", "https://servicos.dpf.gov.br/sinic-certidao/emitirCertidao.html");
		httpPost.addHeader("Connection", "keep-alive");
		

		client.getCookieStore().clear();
		if (sessionId != null) {
			novoCookie(JSESSIONID_COOKIE_KEY, sessionId);
			attachCookiesFromStore(httpPost);
		}
		httpPost.setEntity(asKeyValueEntity(ENCODING,"codeID",codId,"nome",nome,"cpf",(cpf == null ? "" : cpf),"ano","","ci","","dia","","mae","", "mes",//
				"","nacionalidade","","naturalidade","","orgao","","pai","","passaporte","","serie","","ufEmissor","","ufNatural", ""));
		try {
			HttpResponse resp = client.execute(httpPost);
			if (resp.getStatusLine().getStatusCode() == 200 && resp.getEntity().getContentType().getValue().equalsIgnoreCase("application/pdf")) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				baos.close();
				
				ResultadoAntecedentes ra = new ResultadoAntecedentes();
				ra.setPdf(baos.toByteArray());
				ra.setStatus(StatusAntecedentes.SemAntecedentes);
				return ra;
			} else {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				String contentAsString = new String(baos.toByteArray(),ENCODING).replaceAll("[\n\r]","");
				baos.close();
				baos = null;
//				
				ResultadoAntecedentes ra = new ResultadoAntecedentes();
				
				if(contentAsString.matches(".*Não foi possível emitir a Certidão de Antecedentes Criminais com base nos dados informados.*")){
					String codRef = contentAsString.replaceAll("^.*Apresente o seguinte protocolo : ([0-9A-Za-z]*?) .*$", "$1");
					ra.setProtocolo(codRef);
					ra.setStatus(StatusAntecedentes.VerificarComPF);
				}
				return ra;
				
			}
		} catch (Throwable e) {
			log.error("Erro", e);
		} finally {
			httpPost.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);

		}
		return null;
	}
}
