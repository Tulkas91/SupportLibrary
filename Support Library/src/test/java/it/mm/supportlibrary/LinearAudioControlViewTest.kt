package it.mm.supportlibrary

import android.content.Context
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.test.core.app.ApplicationProvider
import it.mm.supportlibrary.ui.component.AudioControlView
import it.mm.supportlibrary.ui.component.LinearAudioControlView
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class LinearAudioControlViewTest {

    private lateinit var linearAudioControlView: LinearAudioControlView
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Mock
    private lateinit var lifecycleOwner: LifecycleOwner

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        linearAudioControlView = LinearAudioControlView(context).apply {
            parentFilePath = "/test/path"
            setLifecycleOwner(lifecycleOwner)
        }
    }

    @Test
    fun `test initial setup`() {
        assertTrue(linearAudioControlView.audioListPath.isEmpty())
        assertEquals(0, linearAudioControlView.count)
    }

    @Test
    fun `test adding new audio control view`() {
        linearAudioControlView.binding.newAudio.performClick()

        assertEquals(1, linearAudioControlView.binding.audioList.childCount)
        assertEquals(1, linearAudioControlView.count)
        assertFalse(linearAudioControlView.binding.tvMessage.isVisible)
    }

    @Test
    fun `test deleting an audio control view`() {
        linearAudioControlView.binding.newAudio.performClick()

        val audioControlView = linearAudioControlView.binding.audioList.getChildAt(0) as AudioControlView
        audioControlView.buttonDelete.performClick()

        assertEquals(0, linearAudioControlView.binding.audioList.childCount)
        assertTrue(linearAudioControlView.binding.tvMessage.isVisible)
    }
}

