package br.com.quintoandar.consultasbr.iptusp;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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
import br.com.quintoandar.consultasbr.pf.RespostaCaptcha;

public class ConsultarIptuCampinas extends SimpleHttpQuerier {
	private static final String ENCODING = "ISO-8859-1";

	private static final Logger log = LoggerFactory.getLogger(ConsultarIptuCampinas.class);

	public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	private static String URL_BASE_IPTU = "http://iptu.campinas.sp.gov.br/iptu";
	private static String URL_CONSULTA_DEBITOS = "http://iptu.campinas.sp.gov.br/iptu/index.html";
	private static String URL_BOLETO = "http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx";
	
	private static String COOKIEID = "JSESSIONID";
	private static String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/34.0.1847.116 Chrome/34.0.1847.116 Safari/537.36";
	

	@Override
	protected boolean shouldPersistCookie(BasicClientCookie ck) {
		return true;
	}
	
	public class ConsultaBoletoResult {
		
		private String retornoHtml = null;
		private List<Integer> parcelasVencida = null;
		
		public ConsultaBoletoResult() {
			super();
		}
		
		public String getRetornoHtml() {
			return retornoHtml;
		}
		public void setRetornoHtml(String retornoHtml) {
			this.retornoHtml = retornoHtml;
		}
		public List<Integer> getParcelasVencida() {
			return parcelasVencida;
		}
		public void setParcelasVencida(List<Integer> parcelasVencida) {
			this.parcelasVencida = parcelasVencida;
		}
		
		
	}
	
	public RespostaCaptcha requestCaptcha() throws IPTUSPException {
		HttpGet httpGet = new HttpGet("http://iptu.campinas.sp.gov.br/iptu/imagecodeRenderer");
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0");

		client.getCookieStore().clear();
		try {
			HttpResponse resp = client.execute(httpGet);
			RespostaCaptcha captchaAnswer = new RespostaCaptcha(getCookieValue(COOKIEID));
			if (resp.getStatusLine().getStatusCode() == 200) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				captchaAnswer = new RespostaCaptcha(getCookieValue(COOKIEID),baos.toByteArray());
			}
			return captchaAnswer;
		} catch (Throwable e) {
			throw new IPTUSPException(e);
		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);

		}
	}
	
	public ConsultaBoletoResult consultaBoletos(String sessionId, String respCaptcha, String codigoCartografico) throws ClientProtocolException, IOException, IPTUSPException {
		
		HttpPost httpIndex = new HttpPost("http://iptu.campinas.sp.gov.br/iptu/index.html");
		httpIndex.setHeader("User-Agent", USER_AGENT);
		httpIndex.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpIndex.setHeader("Accept-Language", "en-US,en;q=0.8");
		httpIndex.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httpIndex.setHeader("Connection", "keep-alive");
		httpIndex.setHeader("Host", "iptu.campinas.sp.gov.br");
		httpIndex.setEntity(asKeyValueEntity("ISO-8859-1", 
				"tipoIdentificacao", "CodigoCartografico",
				"identificador", codigoCartografico,
				"imagecode", respCaptcha
				));

		// httpIndex.addHeader(sessionId.replaceFirst("|.*$", ""), sessionId.replaceFirst("^.*|", ""));
		httpIndex.addHeader(COOKIEID, sessionId);
		novoCookie(COOKIEID, sessionId);
		
		attachCookiesFromStore(httpIndex);
		
		try {
			ConsultaBoletoResult result = new ConsultaBoletoResult();
			
			HttpResponse respIndex = client.execute(httpIndex);
			if (respIndex.getStatusLine().getStatusCode() == 302) { // caso sessão seja criada, server retorna 302; caso não consiga retorna 200 com mensagem na página
				
				HttpGet httpGet = new HttpGet("http://iptu.campinas.sp.gov.br/iptu/imovel.html");
				httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				httpGet.setHeader("Accept-Encoding", "gzip,deflate,sdch");
				httpGet.setHeader("Accept-Language", "en-US,en;q=0.8");
				httpGet.setHeader("Connection", "keep-alive");
				httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded");
				httpGet.setHeader("Host", "iptu.campinas.sp.gov.br");
				httpGet.setHeader("Refer", "http://iptu.campinas.sp.gov.br/iptu/index.html");
				httpGet.setHeader("User-Agent", USER_AGENT);
				
				attachCookiesFromStore(httpGet);
		
				log.info("Acessando consulta IPTU - Prefeitura Campinas");
				HttpResponse respGet = client.execute(httpGet);
				
				if (respGet.getStatusLine().getStatusCode() == 200) {
					
					String htmlBoletos = null;
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					respGet.getEntity().writeTo(baos);
					String htmlParcelas = new String(baos.toByteArray(), "ISO-8859-1");
					
					Document doc = Jsoup.parse(htmlParcelas);				
					Elements idsParcelas = doc.select("[name=idsParcelas]:not([disabled])");
					
					if(idsParcelas != null && idsParcelas.size() > 0) {
						
						
						List<String> formData = new ArrayList<String>();
						
						StringBuilder parcelas = new StringBuilder();
						for(Element parcela : idsParcelas){
							
							if(parcelas.length() > 0){
								parcelas.append(",");
							}
							
							parcelas.append(parcela.val());
						}
						
						formData.add("idsParcelas");
						formData.add(parcelas.toString());						
						formData.add("confirmacao");
						formData.add("true");
						formData.add("tipoBoleto");
						formData.add("carne");
						
						Elements parcelasAtrasadas = doc.select("[name=idsParcelas][onclick]:not([disabled])");
						if(parcelasAtrasadas != null && parcelasAtrasadas.size() > 0){
							
							Elements linhasParcelaAtrasada = doc.select("tr.destaque > td >label");
							if(linhasParcelaAtrasada != null && linhasParcelaAtrasada.size() > 0){
								
								List<Integer> parcelasVencida = new ArrayList<Integer>();
								for(Element el : linhasParcelaAtrasada){
									String parcela = el.text().replaceAll("\\D+"," ");
									if(parcela != null && !parcela.isEmpty()){
										parcelasVencida.add(Integer.parseInt(parcela));
									}
								}
								
								result.setParcelasVencida(parcelasVencida);
							}
							
							Calendar ultimoDiaMes = Calendar.getInstance();
							ultimoDiaMes.set(Calendar.DAY_OF_MONTH, ultimoDiaMes.getActualMaximum(Calendar.DAY_OF_MONTH));
							
							formData.add("dataVencimento");
							formData.add(simpleDateFormat.format(ultimoDiaMes.getTime()));
						}
						
						HttpPost httpCobranca = new HttpPost("http://iptu.campinas.sp.gov.br/iptu/cobranca.html");
						httpCobranca.setHeader("User-Agent", USER_AGENT);
						httpCobranca.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
						httpCobranca.setHeader("Accept-Language", "en-US,en;q=0.8");
						httpCobranca.setHeader("Accept-Encoding", "gzip,deflate,sdch");
						httpCobranca.setHeader("Connection", "keep-alive");
						httpCobranca.setHeader("Host", "iptu.campinas.sp.gov.br");
						httpCobranca.setEntity(asKeyValueEntity("ISO-8859-1", 								
								formData.toArray(new String[formData.size()])
								));
						
						httpCobranca.addHeader(sessionId.replaceFirst("|.*$", ""), sessionId.replaceFirst("^.*|", ""));
						
						attachCookiesFromStore(httpCobranca);
						
							
						HttpResponse respCobranca = client.execute(httpCobranca);
						if (respCobranca.getStatusLine().getStatusCode() == 302) {
							
							HttpGet httpBoletos = new HttpGet("http://iptu.campinas.sp.gov.br/iptu/carne.html");
							httpBoletos.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
							httpBoletos.setHeader("Accept-Encoding", "gzip,deflate,sdch");
							httpBoletos.setHeader("Accept-Language", "en-US,en;q=0.8");
							httpBoletos.setHeader("Connection", "keep-alive");
							httpBoletos.setHeader("Content-Type", "application/x-www-form-urlencoded");
							httpBoletos.setHeader("Host", "iptu.campinas.sp.gov.br");
							httpBoletos.setHeader("Refer", "http://iptu.campinas.sp.gov.br/iptu/carne.html");
							httpBoletos.setHeader("User-Agent", USER_AGENT);
							
							attachCookiesFromStore(httpBoletos);
					
							log.info("Acessando 2a. via de boleto IPTU - Prefeitura Campinas");
							HttpResponse respBoletos = client.execute(httpBoletos);
							
							if (respBoletos.getStatusLine().getStatusCode() == 200) {
							
								ByteArrayOutputStream baosBoletos = new ByteArrayOutputStream();
								respBoletos.getEntity().writeTo(baosBoletos);
								htmlBoletos = new String(baosBoletos.toByteArray(), "ISO-8859-1");
							}
						}
					}
					
					result.setRetornoHtml(htmlBoletos);
					return result;
				} else {
					throw new IPTUSPException("ERRO ao acessar consulta IPTU - Prefeitura Campinas. Código Cartográfico: " + codigoCartografico);
				}
			} else if (respIndex.getStatusLine().getStatusCode() == 200) { // caso não consiga acessar retorna 200 com mensagem na página
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				respIndex.getEntity().writeTo(baos);
				String html = new String(baos.toByteArray(), "ISO-8859-1");
				
				if(html != null){
					
					StringBuilder msg = new StringBuilder("ERRO ao acessar consulta IPTU - Prefeitura Campinas. Código Cartográfico: ");
					msg.append(codigoCartografico);
					msg.append(". - ");
					
					Document doc = Jsoup.parse(html);
					Elements elements = doc.select("span.erro");
					
					if(elements != null && elements.size() > 0){
						String mensagemPMC = elements.get(0).text();				
						if(mensagemPMC != null && !mensagemPMC.isEmpty()){
							msg.append(mensagemPMC);
						}
					}
					
					throw new IPTUSPException(msg.toString());
				}
				
			}
		
		} finally {
			httpIndex.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		client.clearRequestInterceptors();
		
		return null;
	}
	
	public List<Resposta2ViaIPTU> getParcelasIptu(ConsultaBoletoResult consultaBoletos, String codigoCartografico) throws IPTUSPException {
		
		
		List<Resposta2ViaIPTU> parcelas = new ArrayList<Resposta2ViaIPTU>();
		
		String retornoHtml = consultaBoletos.getRetornoHtml();
		if(retornoHtml != null){
			
			Document doc = Jsoup.parse(retornoHtml);
			Elements boletos = doc.select("table.principal");
			
			for(Element boleto : boletos){
				
				Resposta2ViaIPTU p = getParcelaIptu(retornoHtml, codigoCartografico, boleto, consultaBoletos.getParcelasVencida());
				if(p!=null) {
					parcelas.add(p);
				}
			}
		}
		
		return parcelas;
		
	}

	private Resposta2ViaIPTU getParcelaIptu(String htmlBoletos, String codigoCartografico, Element boleto, List<Integer> parcelasVencida) throws IPTUSPException {
		
		Resposta2ViaIPTU res = null;
		
		try {
			
			Elements parcela = boleto.select("table.full > tbody >  tr > td.linhoso[align=center]");
			Integer numParcela = null;
			for(Element el : parcela){
				if(numParcela == null && el.text().trim().toLowerCase().contains("parcela")){
					String campo = el.text().split("/")[0];
					numParcela = Integer.parseInt(campo.replaceAll("\\D+",""));
					break;
				}
			}
			
			Element dataVencmento = boleto.select("tr[height] > td.destaque > b > .fonteMaior").get(0);
			Date vencimentoBoleto = simpleDateFormat.parse(dataVencmento.text());
			
			Element valor = boleto.select("td.linhoso > b > span.fonteMaior").get(0);
			Double valorBoleto = Double.parseDouble(valor.text().replaceAll("[^,0-9]", "").replace(",", ".").trim());
			
			Elements codBarras = boleto.select("tr td[width].linhoso .fonteMaior");
			StringBuilder codigoBoleto = new StringBuilder();
			for(Element el : codBarras){
				codigoBoleto.append(el.text().replaceAll("\\D+",""));
			}
						
			res = new Resposta2ViaIPTU(codigoCartografico,vencimentoBoleto.getYear(),numParcela);
			res.setVencimento(vencimentoBoleto);
			res.setCodigo(codigoBoleto.toString());
			res.setDado(htmlBoletos.getBytes()); //salva carnê inteiro
			res.setValor(valorBoleto);
			
			res.setIsVencida((parcelasVencida != null && parcelasVencida.contains(numParcela)));
		
		} catch (ParseException e) {
			throw new IPTUSPException(e);
		}
		
		return res;
	}
	
	public List<Resposta2ViaIPTU> buscar2aViaIPTU(String sessionId, String respCaptcha, String codigoCartografico) throws IPTUSPException {
		
		List<Resposta2ViaIPTU> result = new  ArrayList<Resposta2ViaIPTU>();
		
		try {
			
			ConsultaBoletoResult consultaBoletos = this.consultaBoletos(sessionId, respCaptcha, codigoCartografico);
				
			if(consultaBoletos != null && consultaBoletos.getRetornoHtml() != null){
				
				result = getParcelasIptu(consultaBoletos, codigoCartografico);
			}
			
		} catch (Throwable e) {
			
			throw new IPTUSPException(e);
		} finally {
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		
		return result;
	}

	public static void main(String[] args) {
		
		ConsultarIptuCampinas crawler = new ConsultarIptuCampinas();
		String htmlRetorno;
		Boolean hasDebitos;
		
		RespostaCaptcha captcha = null;
		try {
			captcha = crawler.requestCaptcha();
		} catch (IPTUSPException e1) {
			e1.printStackTrace();
		}
		try {
			FileOutputStream fos = new FileOutputStream("/home/eduardo/Documents/captcha-iptu-cps.png");
			fos.write(captcha.getCaptchaImage());
			fos.close();
			System.out.println("Imagem captcha salvo.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}