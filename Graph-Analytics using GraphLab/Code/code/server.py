import web
import pymysql
import random
import json

SEL_MAX = "SELECT COUNT(*) FROM map"
SEL_RAN = "SELECT * FROM path WHERE source IN (%s) AND dest IN (%s)"
SEL_NOD = "SELECT map.id, map.name, pagerank.value FROM map, pagerank WHERE map.id = pagerank.id AND map.id IN (%s)"
SEL_TOP = "SELECT pagerank.id, pagerank.value, map.name FROM pagerank, map WHERE pagerank.id = map.id ORDER BY pagerank.value DESC LIMIT %i OFFSET %i"
SEL_ONE = "SELECT * FROM path WHERE source = %i AND dest = %i LIMIT 1"
SEL_ONE_TITLE = "SELECT * FROM path, map m1, map m2 WHERE m1.id = path.source AND m2.id = path.dest AND m1.name LIKE '%s%%' AND m2.name LIKE '%s%%' LIMIT 1"

urls = (
    '/top.json', 'topjson',
    '/path.json', 'pathjson',
    '/path', 'path',
    '/network.json', 'networkjson',
    '/network', 'network',
    '/', 'root',
)

def get_conn():
    return pymysql.connect(
        host='localhost',
        user='cc',
        passwd='password',
        db='cc',
        charset='utf8',
    )

def exec_sql(conn, query):
    cur = conn.cursor()
    cur.execute(query)
    r = cur.fetchall()
    cur.close()
    return r

def get_max(conn):
    return exec_sql(conn, SEL_MAX)[0][0]

def get_rand(conn, cnt):
    mx = get_max(conn)
    return map(lambda x: random.randint(0, mx-1), range(cnt))

def get_rand_paths(conn, dst=10, src=5):
    paths = exec_sql(conn, SEL_RAN % 
        (
            ','.join([str(x) for x in get_rand(conn, src)]),
            ','.join([str(x[0]) for x in exec_sql(conn, SEL_TOP % (dst, random.randint(0, 10 - dst)))])
        )
    )
    paths = [map(int, x[2].split(' ')) for x in paths]
    return paths

def get_nodes_and_links(conn, paths):
    srcs = set()
    dsts = set()
    links = []
    for path in paths:
        for i in xrange(len(path) - 1):
            links.append({'source': path[i], 'target': path[i+1]})
        srcs.add(path[0])
        dsts.add(path[-1])
    
    nodes = set()
    for link in links:
        nodes.add(link['source'])
        nodes.add(link['target'])
    nodes = list(nodes)
    nodes = exec_sql(conn, SEL_NOD % (','.join(map(str, nodes))))
    nodes = map(lambda x: {"label": x[1], "size": x[2], "id": x[0]}, nodes)
    for node in nodes:
        if node['id'] in srcs:
            node['t'] = 'src'
        elif node['id'] in dsts:
            node['t'] = 'dst'
        else:
            node['t'] = 'normal'

    nodemap = {}
    for i in xrange(len(nodes)):
        nodemap[nodes[i]['id']] = i
    for l in links:
        l['source'] = nodemap[l['source']]
        l['target'] = nodemap[l['target']]
    return {'nodes': nodes, 'links': links}

class topjson:
    def GET(self):
        conn = get_conn()
        wi = web.input()
        r = exec_sql(conn, SEL_TOP % (10, 0))
        conn.close()
        web.header('Content-Type', 'application/json')
        web.header('Access-Control-Allow-Origin', '*')
        return json.dumps(r)

class pathjson:
    def GET(self):
        conn = get_conn()
        wi = web.input()
        if 'source' in wi and 'dest' in wi:
            r = exec_sql(conn, SEL_ONE % (int(wi.source), int(wi.dest)))
            r = [map(int, x[2].split(' ')) for x in r]
            r = get_nodes_and_links(conn, r)
        elif 'source_title' in wi and 'dest_title' in wi:
            r = exec_sql(conn, SEL_ONE_TITLE % (wi.source_title, wi.dest_title))
            r = [map(int, x[2].split(' ')) for x in r]
            r = get_nodes_and_links(conn, r)
        else:
            r = get_nodes_and_links(conn, get_rand_paths(conn, dst=1, src=1))
        conn.close()
        web.header('Content-Type', 'application/json')
        web.header('Access-Control-Allow-Origin', '*')
        return json.dumps(r)

class networkjson:
    def GET(self):
        conn = get_conn()
        r = get_nodes_and_links(conn, get_rand_paths(conn, dst=10, src=5))
        conn.close()
        web.header('Content-Type', 'application/json')
        web.header('Access-Control-Allow-Origin', '*')
        return json.dumps(r)

class network:
    def GET(self):
        return web.template.frender('network.html')()

class path:
    def GET(self):
        wi = web.input()
        jsonurl = '/path.json'
        if 'source' in wi and 'dest' in wi:
            jsonurl = '/path.json?source=%s&dest=%s' % (wi.source, wi.dest)
        elif 'source_title' in wi and 'dest_title' in wi:
            jsonurl = '/path.json?source_title=%s&dest_title=%s' % (wi.source_title, wi.dest_title)
        return web.template.frender('path.html')(jsonurl)

class root:
    def GET(self):
        return web.template.frender('root.html')()

if __name__ == "__main__":
    app = web.application(urls, globals())
    app.run()
