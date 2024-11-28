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
    id?: number
    name: string,
    outputs: Output[]
}

export interface Adventure {
    id: number
    name: string,
    scenes: Scene[]
}

export const FilesApi = {

    upload: async (file: File, loop: boolean, volume: number): Promise<SoundFile> => {
        const formData = new FormData();
        formData.append('file', file, file.name.split(".")[0]);
        const it = await fetch(`/files/${file.name.split(".")[0]}?loop=${loop}&volume=${volume}`, {
            method: "PUT",
            body: formData,
        })
        return await it.json()
    },
    find: async (query: string): Promise<SoundFile[]> => {
        return await fetch(`/files/find?query=${query}`).then(it => it.json())
    }
}

export const AdventureApi = {

    create: async (name: string): Promise<Adventure> => {
        return await fetch(`/adventures/${name}`, {method: "POST"}).then(it => it.json())
    },

    assign: async (id: number, scene: Scene): Promise<Adventure> => {
        return await fetch(`/adventures/${id}`,
            {
                method: "PUT",
                headers: {
                    'content-type': 'application/json'
                },
                body: JSON.stringify(scene)
            }
        ).then(it => it.json())
    },
    find: async (query: string): Promise<Adventure[]> => {
        return await fetch(`/adventures/find?query=${query}`).then(it => it.json())
    },
    addFile: async (id: number, outputId: number, fileId: string, playOnStart: boolean): Promise<Adventure[]> => {
        return await fetch(`/adventures/${id}/${outputId}/${fileId}?playOnStart=${playOnStart}`,
            {method: "PUT"}
        ).then(it => it.json())
    },


}