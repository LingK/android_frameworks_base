#
# Copyright (C) 2011 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH:= frameworks/base/data/sounds

# Alarms
PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/alarms/Alarm_Beep_01.ogg:system/media/audio/alarms/Alarm_Beep_01.ogg \
	$(LOCAL_PATH)/alarms/Alarm_Beep_02.ogg:system/media/audio/alarms/Alarm_Beep_02.ogg \
    $(LOCAL_PATH)/alarms/Alarm_Buzzer.ogg:system/media/audio/alarms/Alarm_Buzzer.ogg \
	$(LOCAL_PATH)/alarms/Alarm_Classic.ogg:system/media/audio/alarms/Alarm_Classic.ogg \
	$(LOCAL_PATH)/alarms/Alarm_Beep_03.ogg:system/media/audio/alarms/Alarm_Beep_03.ogg \
    $(LOCAL_PATH)/alarms/Alarm_Rooster_02.ogg:system/media/audio/alarms/Alarm_Rooster_02.ogg

# Effects
PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/effects/camera_click.ogg:system/media/audio/ui/camera_click.ogg \
    $(LOCAL_PATH)/effects/Dock.ogg:system/media/audio/ui/Dock.ogg \
    $(LOCAL_PATH)/effects/Effect_Tick.ogg:system/media/audio/ui/Effect_Tick.ogg \
    $(LOCAL_PATH)/effects/KeypressDelete.ogg:system/media/audio/ui/KeypressDelete.ogg \
    $(LOCAL_PATH)/effects/KeypressReturn.ogg:system/media/audio/ui/KeypressReturn.ogg \
    $(LOCAL_PATH)/effects/KeypressSpacebar.ogg:system/media/audio/ui/KeypressSpacebar.ogg \
    $(LOCAL_PATH)/effects/KeypressStandard.ogg:system/media/audio/ui/KeypressStandard.ogg \
    $(LOCAL_PATH)/effects/Lock.ogg:system/media/audio/ui/Lock.ogg \
    $(LOCAL_PATH)/effects/LowBattery.ogg:system/media/audio/ui/LowBattery.ogg \
    $(LOCAL_PATH)/effects/Undock.ogg:system/media/audio/ui/Undock.ogg \
    $(LOCAL_PATH)/effects/Unlock.ogg:system/media/audio/ui/Unlock.ogg \
    $(LOCAL_PATH)/effects/VideoRecord.ogg:system/media/audio/ui/VideoRecord.ogg

# Notifications
PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/notifications/Alchemy_alert.ogg:system/media/audio/notifications/Alchemy_alert.ogg \
	$(LOCAL_PATH)/notifications/Aldebaran.ogg:system/media/audio/notifications/Aldebaran.ogg \
	$(LOCAL_PATH)/notifications/Altair.ogg:system/media/audio/notifications/Altair.ogg \
	$(LOCAL_PATH)/notifications/Antares.ogg:system/media/audio/notifications/Antares.ogg \
	$(LOCAL_PATH)/notifications/Arcturus.ogg:system/media/audio/notifications/Arcturus.ogg \
	$(LOCAL_PATH)/notifications/Betelgeuse.ogg:system/media/audio/notifications/Betelgeuse.ogg \
	$(LOCAL_PATH)/notifications/Burst_alert.ogg:system/media/audio/notifications/Burst_alert.ogg \
	$(LOCAL_PATH)/notifications/Canopus.ogg:system/media/audio/notifications/Canopus.ogg \
	$(LOCAL_PATH)/notifications/Capella.ogg:system/media/audio/notifications/Capella.ogg \
	$(LOCAL_PATH)/notifications/Castor.ogg:system/media/audio/notifications/Castor.ogg \
	$(LOCAL_PATH)/notifications/CetiAlpha.ogg:system/media/audio/notifications/CetiAlpha.ogg \
	$(LOCAL_PATH)/notifications/Corrosion_alert.ogg:system/media/audio/notifications/Corrosion_alert.ogg \
	$(LOCAL_PATH)/notifications/Cricket.ogg:system/media/audio/notifications/Cricket.ogg \
	$(LOCAL_PATH)/notifications/Current_alert.ogg:system/media/audio/notifications/Current_alert.ogg \
	$(LOCAL_PATH)/notifications/DearDeer.ogg:system/media/audio/notifications/DearDeer.ogg \
	$(LOCAL_PATH)/notifications/Deneb.ogg:system/media/audio/notifications/Deneb.ogg \
	$(LOCAL_PATH)/notifications/Doink.ogg:system/media/audio/notifications/Doink.ogg \
	$(LOCAL_PATH)/notifications/DontPanic.ogg:system/media/audio/notifications/DontPanic.ogg \
	$(LOCAL_PATH)/notifications/Drip.ogg:system/media/audio/notifications/Drip.ogg \
	$(LOCAL_PATH)/notifications/droid.ogg:system/media/audio/notifications/droid.ogg \
	$(LOCAL_PATH)/notifications/Droid_X2_Alert.ogg:system/media/audio/notifications/Droid_X2_Alert.ogg \
	$(LOCAL_PATH)/notifications/Electra.ogg:system/media/audio/notifications/Electra.ogg \
	$(LOCAL_PATH)/notifications/Fomalhaut.ogg:system/media/audio/notifications/Fomalhaut.ogg \
	$(LOCAL_PATH)/notifications/Fragmented_alert.ogg:system/media/audio/notifications/Fragmented_alert.ogg \
	$(LOCAL_PATH)/notifications/Heaven.ogg:system/media/audio/notifications/Heaven.ogg \
	$(LOCAL_PATH)/notifications/Highwire.ogg:system/media/audio/notifications/Highwire.ogg \
	$(LOCAL_PATH)/notifications/KzurbSonar.ogg:system/media/audio/notifications/KzurbSonar.ogg \
	$(LOCAL_PATH)/notifications/Merope.ogg:system/media/audio/notifications/Merope.ogg \
	$(LOCAL_PATH)/notifications/Microcosmosis_alert.ogg:system/media/audio/notifications/Microcosmosis_alert.ogg \
	$(LOCAL_PATH)/notifications/moonbeam.ogg:system/media/audio/notifications/moonbeam.ogg \
	$(LOCAL_PATH)/notifications/Nanobots_alert.ogg:system/media/audio/notifications/Nanobots_alert.ogg \
	$(LOCAL_PATH)/notifications/Nanotubes_alert.ogg:system/media/audio/notifications/Nanotubes_alert.ogg \
	$(LOCAL_PATH)/notifications/Neurologic_alert.ogg:system/media/audio/notifications/Neurologic_alert.ogg \
	$(LOCAL_PATH)/notifications/OnTheHunt.ogg:system/media/audio/notifications/OnTheHunt.ogg \
	$(LOCAL_PATH)/notifications/pizzicato.ogg:system/media/audio/notifications/pizzicato.ogg \
	$(LOCAL_PATH)/notifications/Plastic_Pipe.ogg:system/media/audio/notifications/Plastic_Pipe.ogg \
	$(LOCAL_PATH)/notifications/Polaris.ogg:system/media/audio/notifications/Polaris.ogg \
	$(LOCAL_PATH)/notifications/Pollux.ogg:system/media/audio/notifications/Pollux.ogg \
	$(LOCAL_PATH)/notifications/Procyon.ogg:system/media/audio/notifications/Procyon.ogg \
	$(LOCAL_PATH)/notifications/Reaction_alert.ogg:system/media/audio/notifications/Reaction_alert.ogg \
	$(LOCAL_PATH)/notifications/regulus.ogg:system/media/audio/notifications/regulus.ogg \
	$(LOCAL_PATH)/notifications/Research_alert.ogg:system/media/audio/notifications/Research_alert.ogg \
	$(LOCAL_PATH)/notifications/sirius.ogg:system/media/audio/notifications/sirius.ogg \
	$(LOCAL_PATH)/notifications/Sirrah.ogg:system/media/audio/notifications/Sirrah.ogg \
	$(LOCAL_PATH)/notifications/SpaceSeed.ogg:system/media/audio/notifications/SpaceSeed.ogg \
	$(LOCAL_PATH)/notifications/Tech_alert.ogg:system/media/audio/notifications/Tech_alert.ogg \
	$(LOCAL_PATH)/notifications/tweeters.ogg:system/media/audio/notifications/tweeters.ogg \
	$(LOCAL_PATH)/notifications/vega.ogg:system/media/audio/notifications/vega.ogg \
    $(LOCAL_PATH)/notifications/Vision_alert.ogg:system/media/audio/notifications/Vision_alert.ogg \
    $(LOCAL_PATH)/notifications/Voila.ogg:system/media/audio/notifications/Voila.ogg
		
# Ringtones
PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/ringtones/Alert.ogg:system/media/audio/ringtones/Alert.ogg \
	$(LOCAL_PATH)/ringtones/Alert_Bright.ogg:system/media/audio/ringtones/Alert_Bright.ogg \
	$(LOCAL_PATH)/ringtones/Alert_Natural.ogg:system/media/audio/ringtones/Alert_Natural.ogg \
	$(LOCAL_PATH)/ringtones/Alert_Synthetic.ogg:system/media/audio/ringtones/Alert_Synthetic.ogg \
	$(LOCAL_PATH)/ringtones/AndroMeda.ogg:system/media/audio/ringtones/AndroMeda.ogg \
	$(LOCAL_PATH)/ringtones/Aquila.ogg:system/media/audio/ringtones/Aquila.ogg \
	$(LOCAL_PATH)/ringtones/ArgoNavis.ogg:system/media/audio/ringtones/ArgoNavis.ogg \
	$(LOCAL_PATH)/ringtones/BeatPlucker.ogg:system/media/audio/ringtones/BeatPlucker.ogg \
    $(LOCAL_PATH)/ringtones/Bell_Synthetic.ogg:system/media/audio/ringtones/Bell_Synthetic.ogg \
	$(LOCAL_PATH)/ringtones/BentleyDubs.ogg:system/media/audio/ringtones/BentleyDubs.ogg \
	$(LOCAL_PATH)/ringtones/Big_Easy.ogg:system/media/audio/ringtones/Big_Easy.ogg \
    $(LOCAL_PATH)/ringtones/BirdLoop.ogg:system/media/audio/ringtones/BirdLoop.ogg \
    $(LOCAL_PATH)/ringtones/Bollywood.ogg:system/media/audio/ringtones/Bollywood.ogg \
    $(LOCAL_PATH)/ringtones/Bootes.ogg:system/media/audio/ringtones/Bootes.ogg \
    $(LOCAL_PATH)/ringtones/BussaMove.ogg:system/media/audio/ringtones/BussaMove.ogg \
    $(LOCAL_PATH)/ringtones/Cairo.ogg:system/media/audio/ringtones/Cairo.ogg \
    $(LOCAL_PATH)/ringtones/Calypso_Steel.ogg:system/media/audio/ringtones/Calypso_Steel.ogg \
    $(LOCAL_PATH)/ringtones/CanisMajor.ogg:system/media/audio/ringtones/CanisMajor.ogg \
    $(LOCAL_PATH)/ringtones/CaribbeanIce.ogg:system/media/audio/ringtones/CaribbeanIce.ogg \
    $(LOCAL_PATH)/ringtones/Carina.ogg:system/media/audio/ringtones/Carina.ogg \
    $(LOCAL_PATH)/ringtones/CassioPeia.ogg:system/media/audio/ringtones/CassioPeia.ogg \
    $(LOCAL_PATH)/ringtones/Centaurus.ogg:system/media/audio/ringtones/Centaurus.ogg \
    $(LOCAL_PATH)/ringtones/Chimes_Synthetic.ogg:system/media/audio/ringtones/Chimes_Synthetic.ogg \
    $(LOCAL_PATH)/ringtones/Chrome_Ring.ogg:system/media/audio/ringtones/Chrome_Ring.ogg \
    $(LOCAL_PATH)/ringtones/Club_Cubano.ogg:system/media/audio/ringtones/Club_Cubano.ogg \
    $(LOCAL_PATH)/ringtones/CrayonRock.ogg:system/media/audio/ringtones/CrayonRock.ogg \
    $(LOCAL_PATH)/ringtones/CurveBall.ogg:system/media/audio/ringtones/CurveBall.ogg \
    $(LOCAL_PATH)/ringtones/Cygnus.ogg:system/media/audio/ringtones/Cygnus.ogg \
    $(LOCAL_PATH)/ringtones/DancinFool.ogg:system/media/audio/ringtones/DancinFool.ogg \
    $(LOCAL_PATH)/ringtones/DonMessWivIt.ogg:system/media/audio/ringtones/DonMessWivIt.ogg \
    $(LOCAL_PATH)/ringtones/Doorbell_Synthetic.ogg:system/media/audio/ringtones/Doorbell_Synthetic.ogg \
    $(LOCAL_PATH)/ringtones/Draco.ogg:system/media/audio/ringtones/Draco.ogg \
    $(LOCAL_PATH)/ringtones/Droid_X2.ogg:system/media/audio/ringtones/Droid_X2.ogg \
    $(LOCAL_PATH)/ringtones/Eastern_Sky.ogg:system/media/audio/ringtones/Eastern_Sky.ogg \
    $(LOCAL_PATH)/ringtones/Eridani.ogg:system/media/audio/ringtones/Eridani.ogg \
    $(LOCAL_PATH)/ringtones/EtherShake.ogg:system/media/audio/ringtones/EtherShake.ogg \
    $(LOCAL_PATH)/ringtones/FriendlyGhost.ogg:system/media/audio/ringtones/FriendlyGhost.ogg \
	$(LOCAL_PATH)/ringtones/GameOverGuitar.ogg:system/media/audio/ringtones/GameOverGuitar.ogg \
    $(LOCAL_PATH)/ringtones/Gimme_Mo_Town.ogg:system/media/audio/ringtones/Gimme_Mo_Town.ogg \
    $(LOCAL_PATH)/ringtones/Glacial_Groove.ogg:system/media/audio/ringtones/Glacial_Groove.ogg \
	$(LOCAL_PATH)/ringtones/Growl.ogg:system/media/audio/ringtones/Growl.ogg \
    $(LOCAL_PATH)/ringtones/HalfwayHome.ogg:system/media/audio/ringtones/HalfwayHome.ogg \
    $(LOCAL_PATH)/ringtones/Hydra.ogg:system/media/audio/ringtones/Hydra.ogg \
	$(LOCAL_PATH)/ringtones/InsertCoin.ogg:system/media/audio/ringtones/InsertCoin.ogg \
    $(LOCAL_PATH)/ringtones/Interlude_Synthetic.ogg:system/media/audio/ringtones/Interlude_Synthetic.ogg \
	$(LOCAL_PATH)/ringtones/LoopyLounge.ogg:system/media/audio/ringtones/LoopyLounge.ogg \
	$(LOCAL_PATH)/ringtones/LoveFlute.ogg:system/media/audio/ringtones/LoveFlute.ogg \
    $(LOCAL_PATH)/ringtones/Lyra.ogg:system/media/audio/ringtones/Lyra.ogg \
    $(LOCAL_PATH)/ringtones/Machina.ogg:system/media/audio/ringtones/Machina.ogg \
	$(LOCAL_PATH)/ringtones/MidEvilJaunt.ogg:system/media/audio/ringtones/MidEvilJaunt.ogg \
	$(LOCAL_PATH)/ringtones/MildlyAlarming.ogg:system/media/audio/ringtones/MildlyAlarming.ogg \
	$(LOCAL_PATH)/ringtones/NewPlayer.ogg:system/media/audio/ringtones/NewPlayer.ogg \
	$(LOCAL_PATH)/ringtones/Noises1.ogg:system/media/audio/ringtones/Noises1.ogg \
    $(LOCAL_PATH)/ringtones/No_Limits.ogg:system/media/audio/ringtones/No_Limits.ogg \
	$(LOCAL_PATH)/ringtones/OrganDub.ogg:system/media/audio/ringtones/OrganDub.ogg \
    $(LOCAL_PATH)/ringtones/Organic.ogg:system/media/audio/ringtones/Organic.ogg \
    $(LOCAL_PATH)/ringtones/Orion.ogg:system/media/audio/ringtones/Orion.ogg \
    $(LOCAL_PATH)/ringtones/Paradise_Island.ogg:system/media/audio/ringtones/Paradise_Island.ogg \
    $(LOCAL_PATH)/ringtones/Pattern_Bell.ogg:system/media/audio/ringtones/Pattern_Bell.ogg \
    $(LOCAL_PATH)/ringtones/Pegasus.ogg:system/media/audio/ringtones/Pegasus.ogg \
    $(LOCAL_PATH)/ringtones/Perseus.ogg:system/media/audio/ringtones/Perseus.ogg \
    $(LOCAL_PATH)/ringtones/Playa.ogg:system/media/audio/ringtones/Playa.ogg \
    $(LOCAL_PATH)/ringtones/Provincial_Synthetic.ogg:system/media/audio/ringtones/Provincial_Synthetic.ogg \
    $(LOCAL_PATH)/ringtones/Pyxis.ogg:system/media/audio/ringtones/Pyxis.ogg \
    $(LOCAL_PATH)/ringtones/RadiationOrchestration.ogg:system/media/audio/ringtones/RadiationOrchestration.ogg \
    $(LOCAL_PATH)/ringtones/Revelation.ogg:system/media/audio/ringtones/Revelation.ogg \
    $(LOCAL_PATH)/ringtones/Rigel.ogg:system/media/audio/ringtones/Rigel.ogg \
    $(LOCAL_PATH)/ringtones/Ring_Classic_02.ogg:system/media/audio/ringtones/Ring_Classic_02.ogg \
    $(LOCAL_PATH)/ringtones/Ring_Digital_02.ogg:system/media/audio/ringtones/Ring_Digital_02.ogg \
    $(LOCAL_PATH)/ringtones/Ring_Synth_02.ogg:system/media/audio/ringtones/Ring_Synth_02.ogg \
    $(LOCAL_PATH)/ringtones/Road_Trip.ogg:system/media/audio/ringtones/Road_Trip.ogg \
    $(LOCAL_PATH)/ringtones/RobotsforEveryone.ogg:system/media/audio/ringtones/RobotsforEveryone.ogg \
	$(LOCAL_PATH)/ringtones/RomancingTheTone.ogg:system/media/audio/ringtones/RomancingTheTone.ogg \
    $(LOCAL_PATH)/ringtones/Savannah.ogg:system/media/audio/ringtones/Savannah.ogg \
    $(LOCAL_PATH)/ringtones/Scarabaeus.ogg:system/media/audio/ringtones/Scarabaeus.ogg \
    $(LOCAL_PATH)/ringtones/Sceptrum.ogg:system/media/audio/ringtones/Sceptrum.ogg \
    $(LOCAL_PATH)/ringtones/Seville.ogg:system/media/audio/ringtones/Seville.ogg \
    $(LOCAL_PATH)/ringtones/Sharp.ogg:system/media/audio/ringtones/Sharp.ogg \
    $(LOCAL_PATH)/ringtones/SilkyWay.ogg:system/media/audio/ringtones/SilkyWay.ogg \
	$(LOCAL_PATH)/ringtones/SitarVsSitar.ogg:system/media/audio/ringtones/SitarVsSitar.ogg \
    $(LOCAL_PATH)/ringtones/Solarium.ogg:system/media/audio/ringtones/Solarium.ogg \
    $(LOCAL_PATH)/ringtones/Standard.ogg:system/media/audio/ringtones/Standard.ogg \
	$(LOCAL_PATH)/ringtones/Steppin_Out.ogg:system/media/audio/ringtones/Steppin_Out.ogg \
	$(LOCAL_PATH)/ringtones/Terminated.ogg:system/media/audio/ringtones/Terminated.ogg \
	$(LOCAL_PATH)/ringtones/Testudo.ogg:system/media/audio/ringtones/Testudo.ogg \
    $(LOCAL_PATH)/ringtones/Third_Eye.ogg:system/media/audio/ringtones/Third_Eye.ogg \
    $(LOCAL_PATH)/ringtones/Thunderfoot.ogg:system/media/audio/ringtones/Thunderfoot.ogg \
	$(LOCAL_PATH)/ringtones/TwirlAway.ogg:system/media/audio/ringtones/TwirlAway.ogg \
	$(LOCAL_PATH)/ringtones/UrsaMinor.ogg:system/media/audio/ringtones/UrsaMinor.ogg \
	$(LOCAL_PATH)/ringtones/Vespa.ogg:system/media/audio/ringtones/Vespa.ogg

