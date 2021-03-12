#!/bin/bash

# this script is autogenerated by 'ant startscripts'
# it starts a las2peer node providing the service 'i5.las2peer.services.socialBotManagerService.SocialBotManagerService' of this project
# pls execute it from the root folder of your deployment, e. g. ./bin/start_network.sh

java -cp "lib/*" i5.las2peer.tools.L2pNodeLauncher --port 9020 -o --bootstrap 192.168.178.44:9011 --bootstrap 192.168.178.44:9013 --service-directory service uploadStartupDirectory startService\(\'i5.las2peer.services.socialBotManagerService.SocialBotManagerService@1.0.19\'\) interactive
