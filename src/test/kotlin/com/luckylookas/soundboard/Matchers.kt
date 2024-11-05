package com.luckylookas.soundboard

import com.luckylookas.soundboard.persistence.SoundFile
import com.luckylookas.soundboard.persistence.SoundFileCollection
import org.mockito.ArgumentMatcher

class SoundFileMatcher(val left: SoundFile) : ArgumentMatcher<SoundFile> {
    override fun matches(right: SoundFile): Boolean {
        return right.name == left.name && right.collection.name == left.collection.name;
    }
}

class SoundFileCollectionMatcher(val left: SoundFileCollection) : ArgumentMatcher<SoundFileCollection> {
    override fun matches(right: SoundFileCollection): Boolean {
        return right.name == left.name;
    }
}