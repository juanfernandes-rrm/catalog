# Catalog

Catalog é um serviço do **Nota Social** responsável por centralizar, organizar e disponibilizar informações de produtos e seus preços extraídos de Notas Fiscais de Consumidor Eletrônicas (NFC-e). 
Este serviço tem como objetivo criar um catálogo estruturado e acessível para outros sistemas, facilitando a consulta e o gerenciamento de dados.
Como funciona

  1. Recebimento de Dados: O Catalog consome os dados processados e enviados pelo serviço ReceiptScan por meio de filas de mensagens.
  2. Organização: Os dados recebidos são processados para identificar e consolidar informações sobre produtos, preços, e lojas.
  3. Disponibilização: As informações armazenadas são disponibilizadas ao ecossistema do Nota Social.

## Funcionalidades principais

  - Centralização de dados de produtos e preços extraídos de NFC-e.
  - Cálculo de melhores preço para determinados produtos dentro de uma dada distância.
  - Histórico de preços

## Tecnologias utilizadas

  - Java/Spring para desenvolvimento.
  - RabbitMQ para integração via filas de mensagens.
  - MySQL para persistência das informações.

## Integração com o ecossistema Nota Social

O Catalog desempenha um papel crucial na integração do Nota Social, sendo o ponto central para o armazenamento de dados estruturados de produtos para consumo pelos demais serviços.
