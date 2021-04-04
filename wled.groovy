/**
 *  WLED
 *
 *  Copyright 2020 Shawn Q.
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
    definition (name: "WLED", namespace: "org.uguess", author: "Shawn Q.") {
        capability "Switch"
        capability "Music Player"
        capability "Polling"
        capability "Refresh"

        attribute "color", "String"
        attribute "level", "Number"
        attribute "preset", "Number"
    }

    simulator {
    }

    tiles (scale: 2) {
        standardTile("switch", "device.switch", width: 3, height: 2, canChangeIcon: true) {
            state "on", label:'${name}', action:"off", icon:"st.switches.light.on", backgroundColor:"#00a0dc"
            state "off", label:'${name}', action:"on", icon:"st.switches.light.off", backgroundColor:"#ffffff"
        }

        controlTile("level", "level", "slider",  width: 3, height: 2, inactiveLabel: false,range:"(0..255)" ) {
            state "level", action:"music Player.setLevel", label: "Level"
        }

        standardTile("previous", "device.status", width: 3, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "default", label: "Prev", action: "music Player.previousTrack", icon: "st.Electronics.electronics2", backgroundColor: "#27AE60"
        }

        standardTile("next", "device.status", width: 3, height: 1, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
            state "default", label: "Next", action: "music Player.nextTrack", icon: "st.Electronics.electronics2", backgroundColor: "#27AE60"
        }

        //controlTile("rgbSelector", "color", "color", width: 6, height: 6, inactiveLabel: false) {
        // state "color", action:"setColor"
        //}

        main "switch"
        details([
                "switch","level","previous", "next"
        ])
    }

    preferences {
        input name: "internal_ip", type: "text", title: "Internal IP", required: true
        input name: "internal_port", type: "text", title: "Internal Port", defaultValue: 80, required: true
    }
}

def installed() {
    refresh()
}

def off() {
    sendEvent(name: "switch", value: "off", isStateChange: true)
    //log.debug("Turn OFF")

    return sendGetRequest("/win&T=0")
}
def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true)
    //log.debug("Turn ON, Color:${state.SavedColor}, Brightness: ${state.SavedLevel}, Color2: ${state.SavedColor2}")

    return sendGetRequest("/win&T=1&FX=0" )
}

def setLevel(level) {
    state.level = level.intValue()

    sendEvent(name: "switch", value: "on", isStateChange: true)
    sendEvent(name: "volume", value: state.level, isStateChange: true)

    return sendGetRequest("/win&A=${state.level}")
}

def setColor(value) {
    state.color = value

    //log.debug("Turn ON, Color:${state.SavedColor}, Brightness: ${state.SavedLevel}, Color2: ${state.SavedColor2}")
    //sendEvent(name: "switch", value: "on", isStateChange: true)

    //log.debug("Set Color:${value}")
    def h = (value.hue)*65535/100
    def s = (value.saturation)*255/100

    return sendGetRequest("/win&HU=${h}&SA=${s}")

}

def refresh() {
    sendEvent(name: "status", value: "stopped")

    sendEvent(name: "volume", value: state.level)

    sendEvent(name: "modeRandom", value: false)

    sendEvent(name: "modeRepeat", value: "default")

    sendEvent(name: "modeSingle", value: "default")

    sendEvent(name: "trackTitle", value: "title")

    sendEvent(name: "trackAlbum", value: "album")

    sendEvent(name: "trackArtist", value: "artist")

    sendEvent(name: "trackDescription", value: "desc")
}

def nextTrack() {
    if (state.preset==null){
        state.preset=0;
    }

    state.preset = (state.preset +1)%5

    log.debug("Set Preset:${state.preset}")

    return sendGetRequest("/win&PL=${state.preset}")
}

def previousTrack() {
    if (state.preset==null){
        state.preset=0;
    }

    state.preset = (state.preset + 4)%5

    log.debug("Set Preset:${state.preset}")

    return sendGetRequest("/win&PL=${state.preset}")
}

def toggleRandom(){
}

def toggleRepeat(){
}

def toggleSingle(){
}

def play() {
}

def pause() {
}

def stop() {
}

def playTrack() {
}

def playText() {
}

def mute() {
}

def unmute() {
}

def setTrack() {
}

def resumeTrack() {
}

def restoreTrack() {
}

private sendGetRequest(String url) {
    log.debug("${internal_ip}:${internal_port}${url}")

    def result = new physicalgraph.device.HubAction(
            method: "GET",
            path: "${url}",
            headers: [
                    HOST: "${internal_ip}:${internal_port}"
            ]
    )
    //return result;
}