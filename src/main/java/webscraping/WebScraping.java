package webscraping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class WebScraping {


	public static void main(String[] args) throws ParseException, KeyManagementException, NoSuchAlgorithmException 
	{
		String url = "http://www.infomoney.com.br/mercados/";
		
		try 
		{
			//Acessar URL principal
			Document pagina = conectar(url); 
			
			acessarUltimasNoticias(pagina);
			
		} 
		catch (IOException e) 
		{	
			e.printStackTrace();
		}
		
		
	}
	
	public static Document conectar(String url) throws IOException
	{
		Document pagina = SSLHelper.getConnection(url).userAgent(HttpConnection.DEFAULT_UA).get();
		
		return pagina;
	}
	
	
	public static void acessarUltimasNoticias(Document pagina) throws IOException, ParseException, KeyManagementException, NoSuchAlgorithmException 
	{
		//Pegar a parte de últimas notícias 
		Element noticias = pagina.getElementById("infiniteScroll");
		Elements divsNoticias = noticias.getElementsByClass("hl-title hl-title-2");
		Elements linksNoticias = divsNoticias.select("a[href]");
		
		linksNoticias = proximasPaginas(linksNoticias, 3);
		
		//Acessar as ultimas noticias e exibir as informações
		for (Element link : linksNoticias) 
		{
			String urlNoticia = link.attr("abs:href");
			String tituloNoticia = link.select("a[href][title]").attr("title");
			Document paginaNoticia = conectar(urlNoticia); 
			
			Elements subTituloNoticia = paginaNoticia.getElementsByClass("article-lead");
			Elements autor = paginaNoticia.getElementsByClass("author-name").select("a[href]");
			Elements divData = paginaNoticia.getElementsByClass("article-date");
			Element timeElement = divData.select("time").first();
			
			SimpleDateFormat dateFormatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			SimpleDateFormat dateFormatOutput = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			Date dataInput = dateFormatInput.parse(timeElement.attr("dateTime"));
			String data = dateFormatOutput.format(dataInput);
			
			Elements conteudoNoticia = paginaNoticia.getElementsByClass("article-content");
			
			
			System.out.println("\nUrl: " + urlNoticia + "\n" + 
							   "Titulo: " + tituloNoticia + "\n" +
							   "Subtitulo: " + subTituloNoticia.text()+ "\n" +
							   "Autor: " + autor.text()+ "\n" +
							   "Data: " + data + "\n" +
							   "Conteudo: " + conteudoNoticia.select("p,h2").text());
					
		}
			
	}
	
	public static Elements proximasPaginas(Elements linksNoticias, Integer qtdPaginas) throws KeyManagementException, NoSuchAlgorithmException, IOException
	{
		Integer count = 2;
		
		while(count <= qtdPaginas)
		{
			//Próximas páginas
			String USER_AGENT = "Mozilla/5.0";
			URL urlPróximaPágina = new URL("https://www.infomoney.com.br/?infinity=scrolling");
			String bodyRequest = "action=infinite_scroll&page="+count+"&order=DESC";
			
			fixSSL();
			
			//Requisição para obter o html das próximas páginas
			HttpsURLConnection connection = (HttpsURLConnection) urlPróximaPágina.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("User-Agent", HttpConnection.DEFAULT_UA);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			connection.setDoOutput(true);
			OutputStream os = connection.getOutputStream();
			os.write(bodyRequest.getBytes());
			os.flush();
			os.close();
			
			int responseCodigo = connection.getResponseCode();
			
			
			if(responseCodigo == HttpURLConnection.HTTP_OK)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while((inputLine = in.readLine()) != null)
				{
					response.append(inputLine);
				}
				in.close();
				
				JSONObject jsonResponse = new JSONObject(response.toString());
				String html = jsonResponse.getString("html").toString();
				Element noticias = Jsoup.parse(html);
				Elements divsNoticias = noticias.getElementsByClass("hl-title hl-title-2");
				Elements linksNoticiasAtualizado = divsNoticias.select("a[href]");
				
				for (Element link : linksNoticiasAtualizado) {
					linksNoticias.add(link);
				}
				
			}
			else
			{
				System.err.println("Erro na requisição!");
			}
			
			count++;
		}
		
		return linksNoticias;
	}
	
	public static void fixSSL() throws NoSuchAlgorithmException, KeyManagementException
	{
		
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }

        } };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };
        
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        
	}
}
