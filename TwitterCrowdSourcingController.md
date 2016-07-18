# TwitterCrowdSourcingController
A middleware for ad-hoc real-time crowdsourcing via social media. Bridges between the APIs of popular social media services (Twitter and Facebook to begin with). https://social-computer.github.io

There are three main components of TwitterCrowdSourcingController: Project - Task and TaskRun that are modelled within the database.

## Documentation
This project have different processes that keep taking to eachother through the database.

The processes (all rest in the main package recoin.mongodb_version) are as follows:

1. ProjectCreator (Instantiate projects to be ready for further process)
2. TaskCreator (instantiate projects' tasks for further process)
3. TwitterTaskPerformer (push tasks to Twitter "task")
4. TwitterTaskCollector (pull contributions from Twitter "taskRun")
5. FacebookTaskPerformer (push tasks to Facebook "task")
6. FacebookTaskCollector (pull contributions from Facebook "taskRun")
7. InstructionSetPorcessor (perform actions based on taskRuns)
8. Simulator (simulate contributions based on synthatic data that is produced from various sources)


### Input
For this project to work, there should be two MongoDB databases (these can be modified with the config file) which are: RECOIN\_bins and RECOIN\_projects. These should be instatiated by [RECOINObserver](https://github.com/project-recoin/RECOINObserver).


- RECOIN_projects: it contains all projects.

```
{
  "project_id": 4069159,
  "project_name": "zikavirus",
  "project_start_timestamp": "",
  "project_end_timestamp": "",
  "project_status": "empty",
  "observed": "2016-05-19 17:44:54",
  "bin_id": "zikavirus",
  "identifiers": [
    "zikavirus"
  ],
  "project_type": "validate",
  "task_type": "validate"
}
```

Note that "project_status" : "empty" which will be kick started by the ProjectCreator later.

- RECOIN_bins: this database will have collections as bins. Each bin represents a proejct. Each bin have all raw tasks to be retrived by the TaskCreator later.

```json
{
  "_source": "twitter",
  "id": "733316451554197504",
  "timestamp": "2016-05-19 15:20:41",
  "text": "Fighting the Zika virus with the power of supercomputing https:\/\/t.co\/GAwWUhFC2f #ZikaVirus",
  "screen_name": "robinsnewswire",
  "isRetweet": false,
  "urls": [
    "https:\/\/t.co\/GAwWUhFC2f"
  ],
  "mentions": [
    
  ],
  "hashtags": [
    "ZikaVirus"
  ],
  "geo": {
    
  },
  "status_raw": {
    "filter_level": "low",
    "retweeted": false,
    "in_reply_to_screen_name": null,
    "possibly_sensitive": false,
    "truncated": false,
    "lang": "en",
    "in_reply_to_status_id_str": null,
    "id": "733316451554197504",
    "in_reply_to_user_id_str": null,
    "timestamp_ms": "1463671241068",
    "in_reply_to_status_id": null,
    "created_at": "Thu May 19 15:20:41 +0000 2016",
    "favorite_count": 0,
    "place": null,
    "coordinates": null,
    "text": "Fighting the Zika virus with the power of supercomputing https:\/\/t.co\/GAwWUhFC2f #ZikaVirus",
    "contributors": null,
    "geo": null,
    "entities": {
      "symbols": [
        
      ],
      "urls": [
        {
          "expanded_url": "https:\/\/www.sciencedaily.com\/releases\/2016\/05\/160519081841.htm",
          "indices": [
            57,
            80
          ],
          "display_url": "sciencedaily.com\/releases\/2016\/\u2026",
          "url": "https:\/\/t.co\/GAwWUhFC2f"
        }
      ],
      "hashtags": [
        {
          "text": "ZikaVirus",
          "indices": [
            81,
            91
          ]
        }
      ],
      "user_mentions": [
        
      ]
    },
    "is_quote_status": false,
    "source": "<a href=\"http:\/\/ifttt.com\" rel=\"nofollow\">IFTTT<\/a>",
    "favorited": false,
    "in_reply_to_user_id": null,
    "retweet_count": 0,
    "id_str": "733316451554197504",
    "user": {
      "location": "Flying The Web For News",
      "default_profile": false,
      "profile_background_tile": true,
      "statuses_count": 976794,
      "lang": "en",
      "profile_link_color": "880000",
      "id": 40173650,
      "following": null,
      "protected": false,
      "favourites_count": 589,
      "profile_text_color": "634047",
      "verified": false,
      "description": "Providing trusted world news reports and shopping discounts on the web 24\/7. Retweeting for opinions, useful information, and the many voices of Twitter.",
      "contributors_enabled": false,
      "profile_sidebar_border_color": "FFFFFF",
      "name": "World News Report",
      "profile_background_color": "141106",
      "created_at": "Fri May 15 04:16:20 +0000 2009",
      "default_profile_image": false,
      "followers_count": 23279,
      "profile_image_url_https": "https:\/\/pbs.twimg.com\/profile_images\/2240371734\/robin_digital_stamp_colored_icon2_normal.png",
      "geo_enabled": true,
      "profile_background_image_url": "http:\/\/pbs.twimg.com\/profile_background_images\/13240545\/microsoftocean1.jpg",
      "profile_background_image_url_https": "https:\/\/pbs.twimg.com\/profile_background_images\/13240545\/microsoftocean1.jpg",
      "follow_request_sent": null,
      "url": "http:\/\/www.robinspost.com",
      "utc_offset": -25200,
      "time_zone": "Pacific Time (US & Canada)",
      "notifications": null,
      "profile_use_background_image": true,
      "friends_count": 18960,
      "profile_sidebar_fill_color": "FFFFFF",
      "screen_name": "robinsnewswire",
      "id_str": "40173650",
      "profile_image_url": "http:\/\/pbs.twimg.com\/profile_images\/2240371734\/robin_digital_stamp_colored_icon2_normal.png",
      "listed_count": 3256,
      "is_translator": false
    }
  },
  "media_url": "",
  "wasProcessed": true
}
```


Once these two database have some inputs, this project can be run.


### ProjectCreator
keep watching the "RECOIN\_projects" for any empty projects to be instantiated while maintaining the limit of active project that can be set by the config file. This process keep looping based on a time that can be set from the config file. The same loop logic is done for all processes. Any modified project will be set to "project_status" : "ready" instead of "empty".



### TaskCreator
For any "ready" project, retreive a number of tasks from "bins" in the database "RECOIN\_bins" based on a limit set by the config file to be added to a new collection named "tasks" within database "RECOIN\_projects". 


```json
{
  "publishedAt": "2016-06-17 14:47:30",
  "project_id": 4069159,
  "bin_id_String": "573dfb96e4b0595c813e1775",
  "task_status": "ready",
  "twitter_task_status": "ready",
  "facebook_task_status": "ready",
  "task_text": "#ZikaVirus will hit southern GOP states hard. I agree with @marcorubio  https:\/\/t.co\/xX2GiRrRJS",
  "twitter_url": "https:\/\/twitter.com\/bikka\/status\/733342097055449089",
  "task_type": "validate",
  "priority": 0,
  "pushing_times": 0,
  "embed": {
    "author_name": "bikka",
    "author_url": "https:\/\/twitter.com\/bikka",
    "cache_age": "3153600000",
    "width": 550,
    "html": "<blockquote class=\"twitter-tweet\"><p lang=\"en\" dir=\"ltr\"><a href=\"https:\/\/twitter.com\/hashtag\/ZikaVirus?src=hash\">#ZikaVirus<\/a> will hit southern GOP states hard. I agree with <a href=\"https:\/\/twitter.com\/marcorubio\">@marcorubio<\/a>  <a href=\"https:\/\/t.co\/xX2GiRrRJS\">https:\/\/t.co\/xX2GiRrRJS<\/a><\/p>&mdash; bikka (@bikka) <a href=\"https:\/\/twitter.com\/bikka\/status\/733342097055449089\">May 19, 2016<\/a><\/blockquote>\n<script async src=\"\/\/platform.twitter.com\/widgets.js\" charset=\"utf-8\"><\/script>",
    "provider_url": "https:\/\/twitter.com",
    "type": "rich",
    "provider_name": "Twitter",
    "version": "1.0",
    "url": "https:\/\/twitter.com\/bikka\/status\/733342097055449089",
    "height": null
  },
  "embed_nomedia": {
    "author_name": "bikka",
    "author_url": "https:\/\/twitter.com\/bikka",
    "cache_age": "3153600000",
    "width": 550,
    "html": "<blockquote class=\"twitter-tweet\" data-cards=\"hidden\"><p lang=\"en\" dir=\"ltr\"><a href=\"https:\/\/twitter.com\/hashtag\/ZikaVirus?src=hash\">#ZikaVirus<\/a> will hit southern GOP states hard. I agree with <a href=\"https:\/\/twitter.com\/marcorubio\">@marcorubio<\/a>  <a href=\"https:\/\/t.co\/xX2GiRrRJS\">https:\/\/t.co\/xX2GiRrRJS<\/a><\/p>&mdash; bikka (@bikka) <a href=\"https:\/\/twitter.com\/bikka\/status\/733342097055449089\">May 19, 2016<\/a><\/blockquote>\n<script async src=\"\/\/platform.twitter.com\/widgets.js\" charset=\"utf-8\"><\/script>",
    "provider_url": "https:\/\/twitter.com",
    "type": "rich",
    "provider_name": "Twitter",
    "version": "1.0",
    "url": "https:\/\/twitter.com\/bikka\/status\/733342097055449089",
    "height": null
  },
  "task_id": 1423977,
  "twitter_lastPushAt": "2016-06-19 21:28:56",
  "facebook_lastPushAt": "2016-06-18 23:06:28",
  "facebook_task_id": "964602923577144_1024330604271042"
}
```

Note that "task_status" : "ready", "twitter_task_status" : "ready" and "facebook_task_status" : "ready".



