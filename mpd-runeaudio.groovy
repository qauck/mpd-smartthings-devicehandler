/**
 *  MPD
 *
 *  Copyright 2016 Shawn Q.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "MPD", namespace: "org.uguess", author: "Shawn Q.") {
    	capability "Lock"
    	capability "Switch"
		capability "Music Player"
        capability "Polling"
        capability "Refresh"
        
        attribute "trackTitle", "string"
        attribute "trackAlbum", "string"
        attribute "trackArtist", "string"
	}
    
   	preferences {
      	input("ip", "string", title:"IP Address", description: "MPD Server IP Address", required: true, displayDuringSetup: true)
    }


	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"nowplaying", type:"generic", width:6, height:4) {
        	tileAttribute("device.trackTitle", key: "PRIMARY_CONTROL") {
      			attributeState "default", label: '${currentValue}', icon:"", backgroundColor:"#ffa81e"
    		}
            
            /*
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
      			attributeState "off", label: 'OFF', action: "switch.on", icon: "st.Electronics.electronics16", backgroundColor:"#ffa81e"
                attributeState "on", label: 'ON', action: "switch.off", icon: "st.Electronics.electronics16", backgroundColor:"#ffa81e"
    		}*/
            
            tileAttribute("device.trackDescription", key: "SECONDARY_CONTROL") {
      			attributeState "default", label: '${currentValue}', icon:"", backgroundColor:"#ffa81e"
    		}
  		}
        
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${name}', action: "switch.on", icon: "st.Electronics.electronics16", backgroundColor: "#ffffff"
            state "on", label: '${name}', action: "switch.off", icon: "st.Electronics.electronics16", backgroundColor: "#f39c12"
        }
        
        standardTile("unlock", "device.lock", width: 2, height: 2, canChangeIcon: true) {
            state "locked", label: 'unlock/prev', action: "lock.unlock", icon: "st.Electronics.electronics16", backgroundColor: "#ffffff"
        }
        
        standardTile("lock", "device.lock", width: 2, height: 2, canChangeIcon: true) {
            state "unlocked", label: 'lock/next', action: "lock.lock", icon: "st.Electronics.electronics16", backgroundColor: "#ffffff"
        }
        
        controlTile("volume", "device.volume", "slider", height:2, width:6, range:"(0..100)") {
        	state "volume", action:"music Player.setLevel"
    	}
        
        standardTile("play", "device.status", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Play", action: "music Player.play", icon: "st.Electronics.electronics2", backgroundColor: "#27AE60"
			state "playing", label: "Play", action: "music Player.play", icon: "st.Electronics.electronics2", backgroundColor: "#f39c12"
		}
        
        standardTile("pause", "device.status", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "default", label: "Pause", action: "music Player.pause", icon: "st.Electronics.electronics2", backgroundColor: "#27AE60"
			state "paused", label: "Pause", action: "music Player.pause", icon: "st.Electronics.electronics2", backgroundColor: "#f39c12"
		}

        standardTile("stop", "device.status", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "stopped", label: "Stop", action: "music Player.stop", icon: "st.Electronics.electronics2", backgroundColor: "#27AE60"
		}
        
        standardTile("previous", "device.status", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Prev", action: "music Player.previousTrack", icon: "st.Electronics.electronics2", backgroundColor: "#27AE60"
		}
        
        standardTile("next", "device.status", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Next", action: "music Player.nextTrack", icon: "st.Electronics.electronics2", backgroundColor: "#27AE60"
		}
        
        standardTile("refresh", "device.status", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
			state "default", label: "Refresh", action: "refresh.refresh", icon: "st.secondary.refresh", backgroundColor: "#27AE60"
		}
        
        main "nowplaying"
		details(["nowplaying", "switch", "unlock", "lock", "volume", "play", "pause", "stop", "previous", "next", "refresh"])
	}
}

def installed() {
    refresh()
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
    
    def response = parseLanMessage(description)
        
    if (response.headers){
        log.debug "====headers==="
        log.debug response.headers
    }
    
    if (response.body) {
        log.debug "====body==="
        log.debug response.body
    }
    
    def body = response.body.trim()
    
    if (body == "OK"){
    	refresh()
    	return
    }
    
    def state = parseResponse(body, "state")
    if (state == "stop"){
        sendEvent(name: "status", value: "stopped")
        sendEvent(name: "switch", value: "off");
    }else if (state == "pause"){
        sendEvent(name: "status", value: "paused");
    }else if (state == "play"){
        sendEvent(name: "status", value: "playing");
        sendEvent(name: "switch", value: "on");
    }
    
    def volume = parseResponse(body, "volume")
    if (volume!=null){
    	sendEvent(name: "volume", value: Integer.parseInt(volume));
    }
    
    def songPos = parseResponse(body, "song")
    if (songPos!=null){
    	hubGet("playlistinfo%20" + Integer.parseInt(songPos))
    }
    
    def trackUpdated = false
    
    def title = parseResponse(body, "Title")
    if (title!=null){
    	trackUpdated = true
    	sendEvent(name: "trackTitle", value: title);
    }else{
    	title = parseResponse(body, "file")
        if (title!=null){
        	def idx = title.lastIndexOf('/')
            if (idx != -1){
            	title = title.substring(idx+1)
            }
        	trackUpdated = true
            sendEvent(name: "trackTitle", value: title);
        }
    }
    
    def album = parseResponse(body, "Album")
    if (album!=null){
    	trackUpdated = true
    	sendEvent(name: "trackAlbum", value: album);
    }
    
    def artist = parseResponse(body, "Artist")
    if (artist!=null){
    	trackUpdated = true
    	sendEvent(name: "trackArtist", value: artist);
    }
    
    if (trackUpdated){
    	def desc = device.currentValue("trackArtist") + " - " + device.currentValue("trackAlbum") + "  | " + device.currentValue("status")
       	sendEvent(name: "trackDescription", value: desc);             
    }
        
	// TODO: handle 'status' attribute
	// TODO: handle 'level' attribute
	// TODO: handle 'trackDescription' attribute
	// TODO: handle 'trackData' attribute
	// TODO: handle 'mute' attribute

}

private String parseResponse(response, field){
	def idx = response.indexOf(field + ':')
    log.debug "start idx: " + idx
    if (idx!=-1){
    	def len = field.size()+2
    	def endIdx = response.indexOf("\n", idx + len)
        log.debug "end idx: " + endIdx
        if (endIdx!=-1){
    		return response.substring(idx+len, endIdx).trim()
        }
    }
    return null
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}

private getHostAddress() {
	return "${ip}:80"
}

private hubGet(def apiCommand) {
    def iphex = convertIPtoHex(ip)
    def porthex = convertPortToHex(80)
    device.deviceNetworkId = "$iphex:$porthex"
    log.debug "Device Network Id set to ${iphex}:${porthex}"

	log.debug("Executing hubaction on " + getHostAddress())
    def uri = "/command?cmd=" + apiCommand
    log.debug uri
    
    //def request = """GET ${uri} HTTP/1.1\r\nHOST: ${getHostAddress()}\r\n\r\n"""
    //log.debug request
    //def result = new physicalgraph.device.HubAction(request, physicalgraph.device.Protocol.LAN, "${deviceNetworkId}")
	//result

   	def hubAction = new physicalgraph.device.HubAction(
    	method: "GET",
        path: uri,
        headers: [HOST:getHostAddress(), Accept:"*/*"]
    )
    sendHubCommand(hubAction)
}

// handle commands
def refresh() {
	log.debug "Executing 'refresh'"
    hubGet("status");
}

def off() {
	stop()
}

def on() { 
	play()
}

def lock() {
	nextTrack()
}

def unlock() {
	previousTrack()
}

def play() {
	log.debug "Executing 'play'"
	hubGet("play");
}

def pause() {
	log.debug "Executing 'pause'"
	if (device.currentValue("status")=="paused"){
    	hubGet("pause%200");
    }else{
    	hubGet("pause%201");
    }
}

def stop() {
	log.debug "Executing 'stop'"
	hubGet("stop");
}

def nextTrack() {
	log.debug "Executing 'nextTrack'"
	hubGet("next");
}

def playTrack() {
	log.debug "Executing 'playTrack'"
	// TODO: handle 'playTrack' command
}

def setLevel(level) {
	log.debug "Executing 'setLevel'"
	hubGet("setvol%20" + level.intValue());
}

def playText() {
	log.debug "Executing 'playText'"
	// TODO: handle 'playText' command
}

def mute() {
	log.debug "Executing 'mute'"
	// TODO: handle 'mute' command
}

def previousTrack() {
	log.debug "Executing 'previousTrack'"
	hubGet("previous");
}

def unmute() {
	log.debug "Executing 'unmute'"
	// TODO: handle 'unmute' command
}

def setTrack() {
	log.debug "Executing 'setTrack'"
	// TODO: handle 'setTrack' command
}

def resumeTrack() {
	log.debug "Executing 'resumeTrack'"
	// TODO: handle 'resumeTrack' command
}

def restoreTrack() {
	log.debug "Executing 'restoreTrack'"
	// TODO: handle 'restoreTrack' command
}
