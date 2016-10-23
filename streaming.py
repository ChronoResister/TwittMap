'''
use pip install
python-dateutil
tweepy
certifi
elasticsearch>=1.0.0,<2.0.0

before run this code
'''


import json
import time
from codecs import open
from dateutil import parser

import tweepy
import certifi
from elasticsearch import Elasticsearch

#from tokens import endpoint, consumer_key, consumer_secret, access_token, access_token_secret

#this endpoint has been opened
endpoint = "search-twittmap-jwctb44fx36upptp2wougtkcnm.us-east-1.es.amazonaws.com"

consumer_key = "Skn4yFfeq72HsbsuyByXlHYES"
consumer_secret = "IhqLnUdk6Jq8vmMesaBqXPjC94voSIXxZdpYUnK8eK5t5QgKq7"
access_token = "3707972236-j5c3LKfwNWnUwluylkQcZS0Z2GmUuO3ymh4wgmp"
access_token_secret = "gmLdu0qsvUfY9bbF2Pszlmzs8qK2TkJIgrYj0uHzRpe7f"

def appendlog(f, s):
    f.write(u'[{0}] {1}\n'.format(time.strftime('%Y-%m-%dT%H:%M:%SZ'), s))
    f.flush()


class TwittMapListener(tweepy.StreamListener):
    def __init__(self, es, f):
        super(TwittMapListener, self).__init__()
        self.es = es
        self.f = f

    def on_data(self, data):
        try:
            # Reference: https://dev.twitter.com/overview/api/tweets
            decoded = json.loads(data)
            geo = decoded.get('coordinates')
            if geo:
                geo = geo['coordinates']
                timestamp = parser.parse(decoded['created_at'])
                timestamp = timestamp.strftime('%Y-%m-%dT%H:%M:%SZ')
                tweet = {
                    'user': decoded['user']['screen_name'],
                    'text': decoded['text'],
                    'geo': geo,
                    'time': timestamp
                }
                tweet_id = decoded['id_str']
                self.es.index(index='twittmap', doc_type='tweets', id=tweet_id, body=tweet)
                appendlog(self.f, u'{0}: {1}'.format(tweet_id, json.dumps(tweet, ensure_ascii=False)))
        except Exception as e:
            appendlog(self.f, '{0}: {1}'.format(type(e), str(e)))

    def on_error(self, status):
        if status == 420:  # Rate limited
            appendlog(self.f, 'Error 420')
            return False


if __name__ == '__main__':
    with open('streaming.log', 'a', encoding='utf8') as f:
        appendlog(f, 'Program starts')

        es = Elasticsearch(hosts=[endpoint], port=443, use_ssl=True, verify_certs=True, ca_certs=certifi.where())
        ls = TwittMapListener(es, f)

        auth = tweepy.OAuthHandler(consumer_key, consumer_secret)
        auth.set_access_token(access_token, access_token_secret)

        stream = tweepy.Stream(auth, ls)
        stream.filter(track=["Trump", "Hillary", "Sanders", "Facebook", "LinkedIn",
                             "Amazon", "Google", "Uber", "Debate", "New York"])