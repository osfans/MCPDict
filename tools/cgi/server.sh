ip=`ip addr show eth0 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1`
port=8000
echo http://$ip:$port/cgi-bin/index.py
python2 -m CGIHTTPServer $port
