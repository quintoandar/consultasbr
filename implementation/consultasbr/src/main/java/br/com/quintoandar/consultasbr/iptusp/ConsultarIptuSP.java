package br.com.quintoandar.consultasbr.iptusp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

	private static final NumberFormat numberFormatPTBR = NumberFormat.getInstance(Locale.forLanguageTag("pt-BR"));

	@Override
	protected boolean shouldPersistCookie(BasicClientCookie ck) {
		return true;
	}

	private String getElementValue(Document doc, String elementId) {

		if (doc != null) {
			Elements elements = doc.select("#" + elementId);
			if (elements != null && elements.size() > 0) {
				return elements.get(0).val();
			}
		}

		return null;
	}

	private String consultaDebitos(String numeroContribuinte)
			throws ClientProtocolException, IOException, InterruptedException, IPTUSPException {

		String setor = numeroContribuinte.substring(0, 3);
		String quadra = numeroContribuinte.substring(3, 6);
		String lote = numeroContribuinte.substring(6, 10);
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
				if (htmlGet != null) {

					Document docGet = Jsoup.parse(htmlGet);
					String __VIEWSTATE = getElementValue(docGet, "__VIEWSTATE");
					String __VIEWSTATEGENERATOR = getElementValue(docGet, "__VIEWSTATEGENERATOR");
					String __EVENTVALIDATION = getElementValue(docGet, "__EVENTVALIDATION");

					Thread.sleep(1000);
					HttpPost httpPost = new HttpPost(
							"http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx");
					httpPost.setHeader("Accept",
							"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					httpPost.setHeader("Accept-Encoding", "gzip,deflate");
					httpPost.setHeader("Accept-Language", "en-US,en;q=0.8");
					httpPost.setHeader("Cache-Control", "max-age=0");
					httpPost.setHeader("Connection", "keep-alive");
					httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
					httpPost.setHeader("Host", "www3.prefeitura.sp.gov.br");
					httpPost.setHeader("Origin", "http://www3.prefeitura.sp.gov.br");
					httpPost.setHeader("Referer",
							"http://www3.prefeitura.sp.gov.br/iptudeb3/Forms/iptudeb3_pag01.aspx");
					httpPost.setHeader("User-Agent", USER_AGENT);
					httpPost.setEntity(asKeyValueEntity("UTF-8", "__VIEWSTATE", __VIEWSTATE, "__VIEWSTATEGENERATOR",
							__VIEWSTATEGENERATOR, "__EVENTVALIDATION", __EVENTVALIDATION, "ctl00$MainContent$setor",
							setor, "ctl00$MainContent$quadra", quadra, "ctl00$MainContent$lote", lote,
							"ctl00$MainContent$digito", digito, "ctl00$MainContent$btnPesquisar", "Pesquisar"));

					attachCookiesFromStore(httpPost);

					log.info("Acessando Consulta a débitos - Prefeitura SP Contribuinte {}", numeroContribuinte);
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
				throw new IPTUSPException("ERRO ao consultar débitos - SP. No. Contribuinte: " + numeroContribuinte);
			}

		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		client.clearRequestInterceptors();

		return null;
	}

	private boolean hasDebitos(String htmlConsultaDebitos) {

		if (htmlConsultaDebitos != null) {
			if (!htmlConsultaDebitos.contains("NADA  DEVE  PAGAR")) {
				return true;
			}
		}

		return false;
	}

	private List<Integer> getParcelasAberto(Document doc) {

		List<Integer> parcelasAberto = null;

		if (doc != null) {
			parcelasAberto = new ArrayList<Integer>();
			Elements elements = doc.select("tr > td[align=left] > font[size=2] > b > p");

			if (elements != null && elements.size() > 0) {
				String[] parcelas = elements.get(1).text().replaceAll("\\D+", " ").split(" ");
				for (String s : parcelas) {
					if (s != null && !s.isEmpty()) {
						parcelasAberto.add(Integer.parseInt(s));
					}
				}
			}
		}

		return parcelasAberto;
	}

	private List<Integer> getParcelasVencida(Document doc) {

		List<Integer> parcelasVencida = null;

		if (doc != null) {
			parcelasVencida = new ArrayList<Integer>();
			Elements elements = doc.select("tr > td[align=left][width=75%] > font[size=2] > b");

			if (elements != null && elements.size() > 0) {
				for (Element el : elements) {
					if (el.ownText().trim().contains("PRESTACOES VENCIDAS")) {
						String[] parcelas = el.text().replaceAll("\\D+", " ").split(" ");
						for (String s : parcelas) {
							if (s != null && !s.isEmpty()) {
								parcelasVencida.add(Integer.parseInt(s));
							}
						}
					}
				}
			}
		}

		return parcelasVencida;
	}

	private String consultaBoleto(String numeroContribuinte, Integer parcelaIPTU, Integer anoExercicio)
			throws ClientProtocolException, IOException, IPTUSPException {

		String setor = numeroContribuinte.substring(0, 3);
		String quadra = numeroContribuinte.substring(3, 6);
		String lote = numeroContribuinte.substring(6, 10);
		String digito = numeroContribuinte.substring(10);
		String parcela = String.format("%02d", parcelaIPTU);

		// verifica site
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
				httpPost.setHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				httpPost.setHeader("Accept-Encoding", "gzip,deflate");
				httpPost.setHeader("Accept-Language", "en-US,en;q=0.8");
				httpPost.setHeader("Connection", "keep-alive");
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
				httpPost.setHeader("Host", "www3.prefeitura.sp.gov.br");
				httpPost.setHeader("User-Agent", USER_AGENT);

				httpPost.setEntity(asKeyValueEntity("ISO-8859-1", "txt_contribuinte1", setor, "txt_contribuinte2",
						quadra, "txt_contribuinte3", lote, "txt_contribuinte4", digito, "cmb_parcela", parcela,
						"txt_exercicio", anoExercicio != null ? anoExercicio.toString() : String.valueOf(anoCorrente)));

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
					throw new IPTUSPException(
							"ERRO ao acessar 2a. via de boleto IPTU SP. No. Contribuinte: " + numeroContribuinte);
				}
			}

		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		client.clearRequestInterceptors();

		return null;
	}

	private boolean isValidBoleto(Document doc, String htmlConsultaBoleto) throws IPTUSPException {
		if (doc != null) {
			Elements elements = doc.select("#msg2");

			if (elements != null && elements.size() > 0) {

				String mensagemPMSP = elements.get(0).text();
				if (mensagemPMSP != null && !mensagemPMSP.isEmpty()) {

					StringBuilder msg = new StringBuilder("Erro ao Consultar boleto IPTU SP. ");
					msg.append(mensagemPMSP);

					throw new IPTUSPException(msg.toString(), htmlConsultaBoleto.getBytes(), "text/html");
				}
			}
		}

		return true;
	}

	private Date getVencimentoBoleto(Document doc) throws ParseException {

		Date dataValidade = null;

		if (doc != null) {
			Elements fonts = doc.select("table > tbody > tr > td > font");

			for (Element el : fonts) {
				if (dataValidade == null && el.ownText().trim().equalsIgnoreCase("data de validade")) {
					String dataValidadeText = el.parent().parent().parent().text();
					log.info("Text: " + dataValidadeText);
					dataValidade = sdfRecebido.parse(dataValidadeText.replaceAll("[^0-9/]", ""));
					break;
				}
			}
		}
		return dataValidade;
	}

	private Date getVencimentoOriginalBoleto(Document doc) throws ParseException {

		Date dataVectoOriginal = null;

		if (doc != null) {
			Elements b = doc.select(
					"td[colspan=3]:not([style=border: 0.04cm black solid;]) > table > tbody > tr > td[align=CENTER] > font > b");

			if (b != null && b.size() > 0) {
				String dataVectoOriginalText = b.get(0).text();
				log.info("Text: " + dataVectoOriginal);
				dataVectoOriginal = sdfRecebido.parse(dataVectoOriginalText.replaceAll("[^0-9/]", ""));
			}

		}
		return dataVectoOriginal;
	}

	private String getCodigoBoleto(Document doc) {

		String codigo = null;

		if (doc != null) {
			Elements fonts = doc.select("table > tbody > tr > td > font");

			Pattern p = Pattern.compile("([0-9]{12}) *([0-9]{12}) *([0-9]{12}) *([0-9]{12}) *([0-9]{12})");
			Matcher m;

			for (Element el : fonts) {
				if (codigo == null && (m = p.matcher(el.text())).find()) {
					codigo = m.group(2) + m.group(3) + m.group(4) + m.group(5);
					log.info("Cod: " + codigo);
					break;
				}
			}
		}
		return codigo;
	}

	private Double getValorBase(Document doc) {
		final Integer posicaoCelula = 2;
		return extrairValorColuna(doc, posicaoCelula);
	}

	private Double getMulta(Document doc) {
		final Integer posicaoCelula = 3;
		return extrairValorColuna(doc, posicaoCelula);
	}

	private Double getCorrecaoMonetaria(Document doc) {
		final Integer posicaoCelula = 4;
		return extrairValorColuna(doc, posicaoCelula);
	}

	private Double getJuros(Document doc) {
		final Integer posicaoCelula = 5;
		return extrairValorColuna(doc, posicaoCelula);
	}

	private Double getValorBoleto(Document doc) {
		final Integer posicaoCelula = 7;
		return extrairValorColuna(doc, posicaoCelula);
	}

	/*
	 * Método auxiliar que extraí um valor da coluna de valores dado o HTML e a
	 * posição da célula, em que: 2 - Valor Base (ou principal) 3 - Multa 4 -
	 * Correcao Monetaria 5 - Juros 7 - Valor total
	 */
	private Double extrairValorColuna(Document doc, Integer posicaoCelula) throws NumberFormatException {
		Double valorCampo = 0D;

		if (doc != null) {
			Elements elements = doc.select(String.format("tbody tr:nth-child(%d) td[align=RIGHT]  b", posicaoCelula));

			if (elements != null && elements.size() > 0) {
				String campoStr = elements.get(0).text().replace(".", "").replace(",", ".").replace(" ", "").trim();
				if (!campoStr.isEmpty())
					valorCampo = Double.parseDouble(campoStr);
			}
		}

		return valorCampo;
	}

	private String extrairValorLinha(Element tr, Integer posicaoCelula) throws NumberFormatException {

		if (tr != null) {
			Elements elements = tr.select(String.format("td:nth-child(%d) font b", posicaoCelula));

			if (elements != null && elements.size() > 0) {
				return elements.get(0).text().trim();
			}
		}

		return null;
	}

	private Integer extrairValorInteger(Element tr, Integer posicaoCelula) throws NumberFormatException {
		String strValue = extrairValorLinha(tr, posicaoCelula);
		if (strValue != null) {
			try {
				return Integer.valueOf(strValue);
			} catch (Throwable t) {
				return null;
			}
		}
		return null;
	}

	private Double extrairValorDouble(Element tr, Integer posicaoCelula) throws NumberFormatException {
		String strValue = extrairValorLinha(tr, posicaoCelula);
		if (strValue != null) {
			try {
				return numberFormatPTBR.parse(strValue).doubleValue();
			} catch (Throwable t) {
				return null;
			}
		}
		return null;
	}

	private Set<Integer> extrairListaInteiros(String strValue) {
		Set<Integer> values = null;
		try {
			if (strValue != null) {
				String[] strArray = strValue.replaceAll("\\D+", " ").split(" ");
				if (strArray != null && strArray.length > 0) {
					values = new HashSet<Integer>();
					for (String s : strArray) {
						if (s != null && !s.isEmpty()) {
							values.add(Integer.parseInt(s));
						}
					}
				}
			}
		} catch (Throwable t) {
			return null;
		}
		return values;
	}

	public List<Resposta2ViaIPTU> buscar2aViaIPTU(String codContribuinte, Integer anoExercicio) throws IPTUSPException {
		RespostaConsulta2ViaIptu response =  buscar2aViaIPTU(codContribuinte, anoExercicio, true);
		if(response == null){
			return null;
		}else{
			return response.getList2viaIptus();
		}
	}

	public RespostaConsulta2ViaIptu buscar2aViaIPTUSilently(String codContribuinte, Integer anoExercicio)
			throws IPTUSPException {
		return buscar2aViaIPTU(codContribuinte, anoExercicio, false);
	}

	private RespostaConsulta2ViaIptu buscar2aViaIPTU(String codContribuinte, Integer anoExercicio,
			boolean throwsIptuException) throws IPTUSPException {
		RespostaConsulta2ViaIptu response = new RespostaConsulta2ViaIptu();

		List<Resposta2ViaIPTU> result = new ArrayList<Resposta2ViaIPTU>();

		try {

			String htmlRetorno;

			htmlRetorno = this.consultaDebitos(codContribuinte);
			Document doc = parseDocument(htmlRetorno);

			Boolean hasDebitos = this.hasDebitos(htmlRetorno);

			if (hasDebitos) {

				// busca próximas parcelas
				List<Integer> parcelasAberto = this.getParcelasAberto(doc);
				// procura por lista de parcelas vencidas
				List<Integer> parcelasVencida = this.getParcelasVencida(doc);

				if (parcelasAberto != null && parcelasAberto.size() > 0) {
					for (Integer p : parcelasAberto) {
						getResposta2Via(codContribuinte, anoExercicio, throwsIptuException, response, result,
								parcelasVencida, p);
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (IOException | InterruptedException e) {
			IPTUSPException iptuException = new IPTUSPException(e);
			if (throwsIptuException) {
				throw iptuException;
			} else {
				response.addException(iptuException);
			}
		} finally {
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}

		response.setList2viaIptus(result);
		return response;
	}

	private void getResposta2Via(String codContribuinte, Integer anoExercicio, boolean throwsIptuException,
			RespostaConsulta2ViaIptu response, List<Resposta2ViaIPTU> result, List<Integer> parcelasVencida, Integer p)
			throws IPTUSPException {
		try {
			Resposta2ViaIPTU r = this.getResposta2Via(codContribuinte, p, anoExercicio);
			if (r != null) {
				r.setIsVencida((parcelasVencida != null && parcelasVencida.contains(p)));
				result.add(r);
			}
		} catch (Throwable e) {
			IPTUSPException iptuException = new IPTUSPException(e);
			if (throwsIptuException) {
				throw iptuException;
			} else {
				response.addException(iptuException);
			}
		}
	}

	public Resposta2ViaIPTU buscar2aViaIPTU(String codContribuinte, Integer parcela, Integer anoExercicio)
			throws IPTUSPException {

		Resposta2ViaIPTU result = null;
		try {

			String htmlRetorno = this.consultaDebitos(codContribuinte);
			Boolean hasDebitos = this.hasDebitos(htmlRetorno);
			Document doc = parseDocument(htmlRetorno);
			
			// procura por lista de parcelas vencidas
			List<Integer> parcelasVencida = this.getParcelasVencida(doc);

			if (hasDebitos) {

				result = this.getResposta2Via(codContribuinte, parcela, anoExercicio);
				result.setIsVencida((parcelasVencida != null && parcelasVencida.contains(parcela)));
			}
		} catch (Throwable e) {

			throw new IPTUSPException(e);
		} finally {
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		return result;
	}

	private Resposta2ViaIPTU getResposta2Via(String codContribuinte, Integer parcela, Integer anoExercicio)
			throws ClientProtocolException, IOException, IPTUSPException, ParseException {

		String htmlRetorno = this.consultaBoleto(codContribuinte, parcela, anoExercicio);
		Document doc = parseDocument(htmlRetorno);
		if (this.isValidBoleto(doc, htmlRetorno)) {

			Date vencimentoBoleto = this.getVencimentoBoleto(doc);
			Double valorBoleto = this.getValorBoleto(doc);
			Double valorBase = this.getValorBase(doc);
			Double multa = this.getMulta(doc);
			Double correcaoMonetaria = this.getCorrecaoMonetaria(doc);
			Double juros = this.getJuros(doc);
			String codigoBoleto = this.getCodigoBoleto(doc);
			Date vencimentoOriginal = this.getVencimentoOriginalBoleto(doc);

			Resposta2ViaIPTU res = new Resposta2ViaIPTU(codContribuinte, anoExercicio, parcela);
			res.setVencimento(vencimentoBoleto);
			res.setCodigo(codigoBoleto);
			res.setDado(htmlRetorno.getBytes());
			res.setValor(valorBoleto);
			res.setValorBase(valorBase);
			res.setMulta(multa);
			res.setCorrecaoMonetaria(correcaoMonetaria);
			res.setJuros(juros);
			res.setMesReferencia(vencimentoOriginal);
			return res;
		}

		return null;
	}

	public RespostaConsultaDebito consultarDebitos(String codContribuinte) throws IPTUSPException {
		RespostaConsultaDebito resp = null;
		try {
			String htmlRetorno = this.consultaDebitos(codContribuinte);
			Document doc = parseDocument(htmlRetorno);
			resp = gerarRespostaConsultaDebito(codContribuinte, htmlRetorno, doc, anoCorrente);
		} catch (Throwable t) {
			throw new IPTUSPException(t);
		} finally {
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}

		return resp;
	}

	// VisibleForTesting
	protected RespostaConsultaDebito gerarRespostaConsultaDebito(String codigoContribuinte, String htmlStr, Document doc,
			Integer anoCorrente) {
		RespostaConsultaDebito resposta = null;
		if (htmlStr != null && doc != null) {
			resposta = new RespostaConsultaDebito(codigoContribuinte, anoCorrente);
			resposta.sedtPossuiDebitos(Boolean.FALSE);

			if (this.hasDebitos(htmlStr)) {
				resposta.sedtPossuiDebitos(Boolean.TRUE);
				resposta.setDebitoExercicioAtual(extrairDebitoExercicioAtual(doc));
			}

			if (!htmlStr.contains("Não existem débitos anteriores")) {
				resposta.addAllInformacaoDebito(extrairDebitosAnteriores(doc));
			}
		}
		return resposta;
	}

	private InformacaoDebito extrairDebitoExercicioAtual(Document doc) {
		InformacaoDebito debitoAtual = null;

		Elements elements = doc.select("table  table > tbody > tr");
		if (elements != null && elements.size() >= 5) {
			debitoAtual = new InformacaoDebito();
			debitoAtual.setAnoExercicio(anoCorrente);

			Element linhaTotalDebito = elements.get(0);
			Element linhaParcelasVencidas = elements.get(2);
			Element linhaParcelasAbertas = elements.get(4);

			debitoAtual.setTotalDebito(extrairValorDouble(linhaTotalDebito, 3));

			String linhaVencidasStr = extrairValorLinha(linhaParcelasVencidas, 1);
			Set<Integer> parcelasVencidas = extrairListaInteiros(linhaVencidasStr);
			parcelasVencidas = parcelasVencidas != null ? parcelasVencidas : new HashSet<Integer>();
			debitoAtual.setParcelasVencidas(parcelasVencidas);

			String linhaAbertasStr = linhaParcelasAbertas.select("td:nth-child(1) font b p:nth-child(2)").get(0).text();
			debitoAtual.setParcelasEmAberto(extrairListaInteiros(linhaAbertasStr));

		}
		return debitoAtual;
	}

	private List<InformacaoDebito> extrairDebitosAnteriores(Document doc) {
		List<InformacaoDebito> debitosAnteriores = new ArrayList<InformacaoDebito>();

		Elements linhas = doc.select("table  table table tr:not(:nth-child(2n-1))");

		for (Element linha : linhas) {
			InformacaoDebito infoDebito = processarLinha(linha);
			if (infoDebito != null) {
				debitosAnteriores.add(infoDebito);
			}
		}

		return debitosAnteriores;
	}

	private InformacaoDebito processarLinha(Element linha) {
		Integer exercicio = extrairValorInteger(linha, 1);
		if (exercicio != null) {
			Double valor = extrairValorDouble(linha, 5);
			if (valor != null) {
				String strPrestacoes = extrairValorLinha(linha, 7);
				if (strPrestacoes != null) {
					Set<Integer> prestacoes = extrairListaInteiros(strPrestacoes);
					InformacaoDebito infoDebito = new InformacaoDebito();
					infoDebito.setAnoExercicio(2000 + exercicio);
					infoDebito.setParcelasVencidas(prestacoes);
					infoDebito.setParcelasEmAberto(prestacoes);
					infoDebito.setTotalDebito(valor);
					return infoDebito;
				}
			}
		}
		return null;
	}
	
	protected Document parseDocument(String htmlStr) {
		if (htmlStr != null) {
			htmlStr = htmlStr.replaceAll("[\n\r]", "");
			Document doc = Jsoup.parse(htmlStr.replaceAll("&nbsp;", " "));
			return doc;
		}
		return null;
	}
}