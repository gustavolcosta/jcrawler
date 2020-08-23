# jcrawler

Este é um projeto de um web crawler com a proposta de pegar as últimas notícias da seção Mercado disponibilizadas no site da [Infomoney](https://www.infomoney.com.br/mercados/), exibindo as seguintes informações:

- A URL da notícia;
- O título da notícia;
- O subtítulo da notícia;
- Autor
- A data de publicação no formato (dia/mês/ano hora:minuto);
- O conteúdo da notícia, sem tags html e sem quebras de linha.

O projeto foi todo desenvolvido na linguagem Java utilizando a biblioteca [Jsoup](https://jsoup.org/) para facilitar a extração e manipulação dos dados e da bibloteca org.json para manipulação do JSON nas requisições.

## Instalação e utilização
Para instalação e utilização basta apenas clonar o repositório
```
git clone https://github.com/gustavolcosta/jcrawler.git
```
E realizar a importação do projeto na IDE (Como Eclipse por exemplo), o projeto utiliza o Maven que já realiza a instalação das bibilotecas utilizadas.
