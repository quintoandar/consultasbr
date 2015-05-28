package br.com.quintoandar.consultasbr.iptusp;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.cookie.BasicClientCookie;
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
	
	private static String URL_BASE_IPTU = "http://www3.prefeitura.sp.gov.br/";
	private static String URL_CONSULTA_DEBITOS = "http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx";
	private static String URL_BOLETO = "http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx";
	
	// Nome do Cookie de Login do PHP.
	private static String COOKIEID = "ASP.NET_SessionId";
	
	private static String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/34.0.1847.116 Chrome/34.0.1847.116 Safari/537.36";
	
	private Integer anoCorrente = Calendar.getInstance().get(Calendar.YEAR);

	@Override
	protected boolean shouldPersistCookie(BasicClientCookie ck) {
		return true;
	}
	
	private String getElementValue(Document doc, String elementId){
				
		if(doc != null){
			Elements elements = doc.select("#"+elementId);
			if(elements != null && elements.size() > 0){
				return elements.get(0).val();
			}			
		}
		
		return null;
	}
	
	private String consultaDebitos(String numeroContribuinte) throws ClientProtocolException, IOException, InterruptedException, IPTUSPException {
		
		String setor = numeroContribuinte.substring(0,3);
		String quadra = numeroContribuinte.substring(3,6);
		String lote = numeroContribuinte.substring(6,10);
		String digito = numeroContribuinte.substring(10);
		
		HttpGet httpGet = new HttpGet("http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx");
		httpGet.setHeader("User-Agent", USER_AGENT);
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("Accept-Language", "en-US,en;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Cache-Control", "max-age=0");
		httpGet.setHeader("Host", "www3.prefeitura.sp.gov.br");
		attachCookiesFromStore(httpGet);
		try {
			HttpResponse respGet = client.execute(httpGet);
			if (respGet.getStatusLine().getStatusCode() == 200) {
				
				respGet.setEntity(new GzipDecompressingEntity(respGet.getEntity()));
				ByteArrayOutputStream baosGet = new ByteArrayOutputStream();
				respGet.getEntity().writeTo(baosGet);
				String htmlGet = new String(baosGet.toByteArray(), "ISO-8859-1");
				if(htmlGet != null) {
					
					Document docGet = Jsoup.parse(htmlGet);
					String  __VIEWSTATE = getElementValue(docGet, "__VIEWSTATE");
					String  __VIEWSTATEGENERATOR = getElementValue(docGet, "__VIEWSTATEGENERATOR");
					String  __EVENTVALIDATION = getElementValue(docGet, "__EVENTVALIDATION");
					
					Thread.sleep(1000);
					HttpPost httpPost = new HttpPost("http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx");
					httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					httpPost.setHeader("Accept-Encoding", "gzip,deflate");
					httpPost.setHeader("Accept-Language", "en-US,en;q=0.8");
					httpPost.setHeader("Cache-Control", "max-age=0");
					httpPost.setHeader("Connection", "keep-alive");
					httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
					httpPost.setHeader("Host", "www3.prefeitura.sp.gov.br");
					httpPost.setHeader("Origin", "http://www3.prefeitura.sp.gov.br");
					httpPost.setHeader("Referer", "http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx");
					httpPost.setHeader("User-Agent", USER_AGENT);
					httpPost.setEntity(asKeyValueEntity("UTF-8", 
							"__VIEWSTATE", __VIEWSTATE, 
							"__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR, 
							"__EVENTVALIDATION", __EVENTVALIDATION, 
							"ctl00$MainContent$setor", setor,
							"ctl00$MainContent$quadra", quadra,
							"ctl00$MainContent$lote", lote,
							"ctl00$MainContent$digito", digito,
							"ctl00$MainContent$btnPesquisar", "Pesquisar"));
					
					attachCookiesFromStore(httpPost);
	
					log.info("Acessando Consulta a débitos - Prefeitura SP");
					HttpResponse respPost = client.execute(httpPost);
					
					if (respPost.getStatusLine().getStatusCode() == 200) {
						
						respPost.setEntity(new GzipDecompressingEntity(respPost.getEntity()));		
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						respPost.getEntity().writeTo(baos);
						String html = new String(baos.toByteArray(), "ISO-8859-1");
						
						return html;
					}
				}
			} else {
				throw new IPTUSPException("ERRO ao consultar débitos. No. Contribuinte: " + numeroContribuinte);
			}
				
		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		client.clearRequestInterceptors();
		
		return null;
	}
	
	private boolean hasDebitos(String htmlConsultaDebitos){
		
		if(htmlConsultaDebitos != null){
			if(!htmlConsultaDebitos.contains("NADA  DEVE  PAGAR")){
				return true;
			} 
		}
		
		return false;
	}
	
	private List<Integer> getParcelasAberto(String htmlConsultaDebitos){
		
		List<Integer> parcelasAberto = null;
		
		if(htmlConsultaDebitos != null){
			parcelasAberto = new ArrayList<Integer>();
			Document doc = Jsoup.parse(htmlConsultaDebitos);
			Elements elements = doc.select("tr > td[align=left] > font[size=2] > b > p");

			if(elements != null && elements.size() > 0){
				String[] parcelas = elements.get(1).text().replaceAll("\\D+"," ").split(" ");
				for(String s : parcelas){
					if(s != null && !s.isEmpty()){
						parcelasAberto.add(Integer.parseInt(s));
					}
				}
			}
		}
		
		return parcelasAberto;
	}
	
	private String consultaBoleto(String numeroContribuinte, Integer parcelaIPTU, Integer anoExercicio) throws ClientProtocolException, IOException, IPTUSPException {
		
		String setor = numeroContribuinte.substring(0,3);
		String quadra = numeroContribuinte.substring(3,6);
		String lote = numeroContribuinte.substring(6,10);
		String digito = numeroContribuinte.substring(10);
		String parcela = String.format("%02d", parcelaIPTU);
		
		
		//verifica site
		HttpGet httpGet = new HttpGet("http://www3.prefeitura.sp.gov.br/iptusimp/index.html");
		httpGet.setHeader("User-Agent", USER_AGENT);
		httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpGet.setHeader("Accept-Language", "en-US,en;q=0.8");
		httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httpGet.setHeader("Connection", "keep-alive");
		httpGet.setHeader("Host", "www3.prefeitura.sp.gov.br");
		attachCookiesFromStore(httpGet);
		
		try {
			HttpResponse respGet = client.execute(httpGet);
			if (respGet.getStatusLine().getStatusCode() == 200) {
				
				HttpPost httpPost = new HttpPost("http://www3.prefeitura.sp.gov.br/iptusimp/index.jsp");
				httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				httpPost.setHeader("Accept-Encoding", "gzip,deflate");
				httpPost.setHeader("Accept-Language", "en-US,en;q=0.8");
				httpPost.setHeader("Connection", "keep-alive");
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
				httpPost.setHeader("Host", "www3.prefeitura.sp.gov.br");
				httpPost.setHeader("User-Agent", USER_AGENT);
				
				httpPost.setEntity(asKeyValueEntity("ISO-8859-1", 
						"txt_contribuinte1", setor,
						"txt_contribuinte2", quadra,
						"txt_contribuinte3", lote,
						"txt_contribuinte4", digito,
						"cmb_parcela", parcela,
						"txt_exercicio", anoExercicio != null ? anoExercicio.toString() : String.valueOf(anoCorrente)
						));
				
				attachCookiesFromStore(httpPost);
		
				log.info("Acessando 2a. via de boleto IPTU - Prefeitura SP");
				HttpResponse respPost = client.execute(httpPost);
				
				if (respPost.getStatusLine().getStatusCode() == 200) {
					
					respPost.setEntity(new GzipDecompressingEntity(respPost.getEntity()));		
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					respPost.getEntity().writeTo(baos);
					String html = new String(baos.toByteArray(), "ISO-8859-1");
					
					return html;
							
						
				} else {
					throw new IPTUSPException("ERRO ao acessar 2a. via de boleto IPTU . No. Contribuinte: " + numeroContribuinte);
				}
			}
		
		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		client.clearRequestInterceptors();
		
		return null;
	}
	
	private boolean isValidBoleto(String htmlConsultaBoleto) throws IPTUSPException{
		
		if(htmlConsultaBoleto != null){
			Document doc = Jsoup.parse(htmlConsultaBoleto);
			Elements elements = doc.select("#msg2");
			
			if(elements != null && elements.size() > 0){
				
				String mensagemPMSP = elements.get(0).text();				
				if(mensagemPMSP != null && !mensagemPMSP.isEmpty()){
						
					StringBuilder msg = new StringBuilder("Erro ao Consultar boleto IPTU. ");
					msg.append(mensagemPMSP);
							
					throw new IPTUSPException(msg.toString(), htmlConsultaBoleto.getBytes(), "text/html");
				} 
			}
		}
		
		return true;
	}
	
	private Double getValorBoleto(String htmlConsultaBoleto){
		
		Double valorBoleto = 0D;
		
		if(htmlConsultaBoleto != null){
			Document doc = Jsoup.parse(htmlConsultaBoleto);
			Elements elements = doc.select("tr > td[align=RIGHT] > font > b");

			if(elements != null && elements.size() > 0){
				valorBoleto = Double.parseDouble(elements.get(0).text().replace(".", "").replace(",", ".").replace(" ","").trim());
			}
		}
		
		return valorBoleto;
	}
	
	private Date getVencimentoBoleto(String htmlConsultaBoleto) throws ParseException{
		
		Date dataValidade = null;
		
		if(htmlConsultaBoleto != null){
			
			htmlConsultaBoleto = htmlConsultaBoleto.replaceAll("[\n\r]", "");
			Document doc = Jsoup.parse(htmlConsultaBoleto.replaceAll("&nbsp;", " "));
			Elements fonts = doc.select("table > tbody > tr > td > font");
			
			for(Element el : fonts){
				if(dataValidade == null && el.ownText().trim().equalsIgnoreCase("data de validade")){
					String dataValidadeText = el.parent().parent().parent().text();
					log.info("Text: "+dataValidadeText);
					dataValidade  = sdfRecebido.parse(dataValidadeText.replaceAll("[^0-9/]", ""));
					break;
				}  
			}
		}
		return dataValidade;
	}
	
	private String getCodigoBoleto(String htmlConsultaBoleto){
		
		String codigo = null;
		
		if(htmlConsultaBoleto != null){
			htmlConsultaBoleto = htmlConsultaBoleto.replaceAll("[\n\r]", "");
			
			Document doc = Jsoup.parse(htmlConsultaBoleto.replaceAll("&nbsp;", " "));
			
			Elements fonts = doc.select("table > tbody > tr > td > font");
			
			Pattern p = Pattern.compile("([0-9]{12}) *([0-9]{12}) *([0-9]{12}) *([0-9]{12}) *([0-9]{12})");
			Matcher m;
			
			for(Element el : fonts){
				if(codigo == null && (m = p.matcher(el.text())).find()){
					codigo =m.group(2)+m.group(3)+m.group(4)+m.group(5);
					log.info("Cod: "+codigo);
					break;
				} 
			}
		}
		return codigo;
	}
	
	public List<Resposta2ViaIPTU> buscar2aViaIPTU(String codContribuinte, Integer anoExercicio) throws IPTUSPException {
		
		List<Resposta2ViaIPTU> result = new  ArrayList<Resposta2ViaIPTU>();
		
		try {
			
			String htmlRetorno = this.consultaDebitos(codContribuinte);
			Boolean hasDebitos = this.hasDebitos(htmlRetorno);	
			
			if (hasDebitos) {
				
				//busca próximas parcelas
				List<Integer> parcelasAberto = this.getParcelasAberto(htmlRetorno);
				
				if(parcelasAberto != null && parcelasAberto.size() > 0){
					
					for (Integer p : parcelasAberto) {
				
						htmlRetorno = this.consultaBoleto(codContribuinte, p, anoExercicio);
							
						if(this.isValidBoleto(htmlRetorno)) {
							
							Date vencimentoBoleto = this.getVencimentoBoleto(htmlRetorno);
							Double valorBoleto = this.getValorBoleto(htmlRetorno);
							String codigoBoleto = this.getCodigoBoleto(htmlRetorno);
										
							Resposta2ViaIPTU res = new Resposta2ViaIPTU(codContribuinte,anoExercicio, p);
							res.setVencimento(vencimentoBoleto);
							res.setCodigo(codigoBoleto);
							res.setDado(htmlRetorno.getBytes());
							res.setValor(valorBoleto);
							
							result.add(res);
						}
					}
				} else{
					return null;
				}
			} else {
				return null;
			}
		} catch (Throwable e) {
			
			throw new IPTUSPException(e);
		} finally {
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		
		return result;
	}

	public Resposta2ViaIPTU buscar2aViaIPTU(String codContribuinte, Integer parcela, Integer anoExercicio) throws IPTUSPException {
		
		try {
			
			String htmlRetorno = this.consultaDebitos(codContribuinte);
			Boolean hasDebitos = this.hasDebitos(htmlRetorno);	
			
			if(hasDebitos) {
				
				htmlRetorno = this.consultaBoleto(codContribuinte, parcela, anoExercicio);
				
				if(this.isValidBoleto(htmlRetorno)) {
					
					Date vencimentoBoleto = this.getVencimentoBoleto(htmlRetorno);
					Double valorBoleto = this.getValorBoleto(htmlRetorno);
					String codigoBoleto = this.getCodigoBoleto(htmlRetorno);
								
					Resposta2ViaIPTU res = new Resposta2ViaIPTU(codContribuinte,anoExercicio,parcela);
					res.setVencimento(vencimentoBoleto);
					res.setCodigo(codigoBoleto);
					res.setDado(htmlRetorno.getBytes());
					res.setValor(valorBoleto);
					
					return res;
				}
			}
		} catch (Throwable e) {
			
			throw new IPTUSPException(e);
		} finally {
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		return null;
	}
	
	public static void main(String[] args) {
		
		ConsultarIptuSP crawler = new ConsultarIptuSP();
		String htmlRetorno;
		Boolean hasDebitos;
		
		try {
			crawler.buscar2aViaIPTU("010.078.0045-3".replaceAll("\\D+",""), 5, 2014);
		}catch (Throwable t){
			System.out.println("Erro testes. " + t.getMessage() + t.getStackTrace());
		}
		
		try {
			//Teste - possui débito
			String quitado = "041.164.0382-2";
			quitado = quitado.replaceAll("\\D+","");
			htmlRetorno = crawler.consultaDebitos(quitado);
			hasDebitos = crawler.hasDebitos(htmlRetorno);
			System.out.println("Contribuinte Quitado. " + hasDebitos);
			
			String isento = "042.019.0079-7";
			isento = isento.replaceAll("\\D+","");
			htmlRetorno = crawler.consultaDebitos(isento);
			hasDebitos= crawler.hasDebitos(htmlRetorno);
			System.out.println("Contribuinte Isento. " + hasDebitos);
			
			String comDebito = "013.029.0819-7";
			comDebito = comDebito.replaceAll("\\D+","");
			htmlRetorno = crawler.consultaDebitos(comDebito);
			hasDebitos = crawler.hasDebitos(htmlRetorno);		
			System.out.println("Contribuinte com débitos. " + hasDebitos);
			
			if(hasDebitos){
				//Teste - busca próxima parcela
				List<Integer> proxsParcelas = crawler.getParcelasAberto(htmlRetorno);
				System.out.println("Próx. Parcela " + proxsParcelas);
				
				//Teste - busca 2a. via parcela
				htmlRetorno = crawler.consultaBoleto(comDebito,proxsParcelas.get(0), null);
				
				if(crawler.isValidBoleto(htmlRetorno)){
					Double valorBoleto = crawler.getValorBoleto(htmlRetorno);
					System.out.println("Valor Boleto = " + valorBoleto);
					
					Date vencimentoBoleto = crawler.getVencimentoBoleto(htmlRetorno);
					System.out.println("Vencimento Boleto = " + vencimentoBoleto);
					
					String codigoBoleto = crawler.getCodigoBoleto(htmlRetorno);
					System.out.println("Código Boleto = " + codigoBoleto);
				}
				
			}
		} catch (Throwable t){
			System.out.println("Erro testes. " + t.getMessage() + t.getStackTrace());
		}
	}
}