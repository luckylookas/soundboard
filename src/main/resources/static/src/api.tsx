
export interface SoundFile {
    name: string,
    loop: boolean,
    volume: number
    id: number
}

export interface SoundDevice {
    id: number
    name: string,
    volume: number
    currentlyPlaying?: SoundFile
}

export interface Output {
    id: number
    name: string,
    files: SoundFile[]
    playOnStart?: SoundFile
    devices: SoundDevice[]
}

export interface Scene {
    id: number
    name: string,
    output: Output[]
}

export interface Adventure {
    id: number
    name: string,
    scenes: Scene[]
}

export const Library = {

    upload: async (file: File, loop: boolean, volume: number): Promise<SoundFile> => {
        const formData  = new FormData();
        formData.append('file', file, file.name.split(".")[0]);
        const it = await fetch(`/files/${file.name.split(".")[0]}?loop=${loop}&volume=${volume}`, {
            method: "PUT",
            body: formData,
        })
        return await it.json()
    },
}

