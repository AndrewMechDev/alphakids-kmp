import AVFoundation

final class AudioPlayerService: ObservableObject {
    private var player: AVPlayer?
    @Published var isPlaying = false

    func play(url: URL) {
        let item = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: item)
        player?.play()
        isPlaying = true

        NotificationCenter.default.addObserver(
            forName: .AVPlayerItemDidPlayToEndTime,
            object: item,
            queue: .main
        ) { [weak self] _ in
            self?.isPlaying = false
        }
    }

    func stop() {
        player?.pause()
        player = nil
        isPlaying = false
    }
}
