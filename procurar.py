import os

def buscar_em_arquivo(caminho, texto):
    try:
        with open(caminho, 'r', encoding='utf-8') as arquivo:
            conteudo = arquivo.read()
            if texto in conteudo:
                return True
    except Exception as e:
        print(f"Erro ao ler o arquivo {caminho}: {e}")
    return False

def buscar_em_pasta(pasta, texto):
    for pasta_atual, subpastas, arquivos in os.walk(pasta):
        for arquivo in arquivos:
            caminho_completo = os.path.join(pasta_atual, arquivo)
            if buscar_em_arquivo(caminho_completo, texto):
                print(f'Texto encontrado em {caminho_completo}')

# Exemplo de uso
pasta_inicial = '.'
texto_desejado = '127.0.0.1'

buscar_em_pasta(pasta_inicial, texto_desejado)
