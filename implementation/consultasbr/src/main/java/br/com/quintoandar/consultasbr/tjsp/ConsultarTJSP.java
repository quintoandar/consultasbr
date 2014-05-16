package br.com.quintoandar.consultasbr.tjsp;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.quintoandar.consultasbr.core.SimpleHttpQuerier;

public class ConsultarTJSP extends SimpleHttpQuerier {
	private static final String ENCODING = "ISO-8859-1";

	private static final Logger log = LoggerFactory.getLogger(ConsultarTJSP.class);

	public static SimpleDateFormat sdfRecebido = new SimpleDateFormat("dd/MM/yyyy");

	public ResultadoConsulta consultarNome(String nome, boolean porNomeCompleto) {
		return consultarNome(nome, true, 1);
	}

	public ResultadoConsulta consultarNome(String nome, boolean porNomeCompleto, Integer pagina) {
		Map<String, String> map = new HashMap<String, String>();

		map.put("paginaConsulta", pagina.toString());
		map.put("localPesquisa.cdLocal", "-1");
		map.put("cbPesquisa", "NMPARTE");
		map.put("tipoNuProcesso", "UNIFICADO");
		map.put("dePesquisa", nome);
		if (porNomeCompleto) {
			map.put("chNmCompleto", "true");
		}
		String searchUrl = "http://esaj.tjsp.jus.br/cpo/pg/search.do" + asQueryParams(map, ENCODING);
		HttpGet httpGet = new HttpGet(searchUrl);
		httpGet.addHeader("Accept", "text/html");
		httpGet.addHeader("Accept-Encoding", "identity");
		httpGet.addHeader("Accept-Language", "en-US,en;q=0.8");
		httpGet.addHeader("Connection", "keep-alive");

		ResultadoConsulta resCons = new ResultadoConsulta();
		resCons.setUrlBusca(searchUrl);
		resCons.setPagina(pagina);
		// List<ResultadosTJSP> resultados = new LinkedList<ResultadosTJSP>();

		try {
			HttpResponse resp = client.execute(httpGet);
			if (resp.getStatusLine().getStatusCode() == 200) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resp.getEntity().writeTo(baos);
				String html = new String(baos.toByteArray(), ENCODING);
				baos.close();
				baos = null;

				resCons.setPdf(getHtmlWithBase(html, ENCODING,"http://esaj.tjsp.jus.br"));

				Document doc = Jsoup.parse(html.replaceAll("&nbsp;", " "));
				Elements els = doc.select("div#listagemDeProcessos > div");
				String foro = null;
				for (Element e : els) {
					if (e.attr("id") != null && e.attr("id").trim().contains("divProcesso")) {
						ResultadosTJSP res = new ResultadosTJSP(searchUrl, foro);
						Element nuProcesso = e.select("div.nuProcesso").get(0);

						StringBuilder tipoBuild = new StringBuilder();
						for (Node nodeSib : nuProcesso.siblingNodes()) {
							if (nodeSib instanceof TextNode) {
								tipoBuild.append(nodeSib.toString());
							}
						}
						res.setTipo(StringEscapeUtils.unescapeHtml4(tipoBuild.toString()).trim());

						Elements nuProcSiblings = nuProcesso.siblingElements().select("div.espacamentoLinhas");
						String envNome = nuProcSiblings.get(0).text();
						res.setEnvolvimento(envNome.substring(0, envNome.indexOf(':')).trim());
						res.setNome(envNome.substring(envNome.indexOf(':') + 1, envNome.length()).trim());

						String dataEVara = nuProcSiblings.get(1).text().replaceAll("^.*?:", "");
						res.setRecebidoEm(sdfRecebido.parse(dataEVara.split("-")[0].trim()));
						res.setVara(dataEVara.split("-")[1].trim());

						Element link = nuProcesso.select("a.linkProcesso").get(0);
						res.setCodigoProcesso(link.ownText());
						res.setUrlDetalhes("http://esaj.tjsp.jus.br" + link.attr("href"));
						if (link.siblingElements().size() > 0) {
							Element extra = link.siblingElements().get(0);
							res.setCodigoExtra(extra.ownText().replaceAll("[()]", ""));
						}

						resCons.add(res);
					} else {
						Elements title = e.select("table > tbody > tr > td");
						if (title.size() > 0 && title.get(0).hasText()) {
							foro = title.get(0).ownText();
						}

					}
				}

				// if (htmlClean.matches("^.*ai/verify/[0-9]*[^0-9].*$")) {
				// String numb =
				// htmlClean.replaceAll("^.*ai/(action|verify)/([0-9]*)[^0-9].*$",
				// "$2");
				// if (!StringUtils.isNullEmptyOrBlank(numb)) {
				// this.formId = new Integer(numb);
				// }
				// }
				// return html;
			}
		} catch (Throwable e) {
			log.error("Erro", e);
			// throw new
			// ServicoExternoException("Error chamarUrlESeguirParaLocationSeHouver",
			// e);
		} finally {
			httpGet.abort();
			connMan.closeIdleConnections(1, TimeUnit.MILLISECONDS);
		}
		return resCons;

	}
}
