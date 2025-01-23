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
    id?: number
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

export const GameApi = {

    stop: async (outputId: number) => {
        return await fetch(`/game/${outputId}`, {method: "DELETE"})
    },
    play: async (outputId: number, fileId?: number) => {
        return await fetch(`/game/${outputId}${fileId ? `?fileId=${fileId}`: ''}`, {method: "POST"})
    }
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
    get: async (id: number): Promise<Adventure> => {
        return await fetch(`/adventures/${id}`).then(it => it.json())
    },
    create: async (name: string): Promise<Adventure> => {
        return await fetch(`/adventures/${name}`, {method: "POST"}).then(it => it.json())
    },

    removeOutput: async (adventureId: number, sceneId: number, outputId: number): Promise<Adventure> => {
        return await fetch(`/adventures/${adventureId}/${sceneId}/${outputId}`, {
            method: "DELETE"

        }).then(it => it.json())
    },

    addOutput: async (adventureId: number, sceneId: number, output: Output): Promise<Adventure> => {
        return await fetch(`/adventures/${adventureId}/${sceneId}`, {
            headers: {
                'content-type': 'application/json'
            },
            body: JSON.stringify(output),
            method: "PUT"

        }).then(it => it.json())
    },

    createScene: async (id: number, scene: Scene): Promise<Adventure> => {
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

    removeScene: async (id: number, sceneId: number): Promise<any> => {
        return await fetch(`/adventures/${id}/${sceneId}`,
            {
                method: "DELETE",

            }
        ).then(it => it.json())
    },
    find: async (query: string): Promise<Adventure[]> => {
        return await fetch(`/adventures/find?query=${query}`).then(it => it.json())
    },

    addFile: async (id: number, sceneId: number, outputId: number, fileId: number, playOnStart: boolean): Promise<Adventure[]> => {
        return await fetch(`/adventures/${id}/${sceneId}/${outputId}/${fileId}?playOnStart=${playOnStart}`,
            {method: "PUT"}
        ).then(it => it.json())
    },

    removeFile: async (id: number, sceneId: number, outputId: number, fileId: number): Promise<Adventure[]> => {
        return await fetch(`/adventures/${id}/${sceneId}/${outputId}/${fileId}`,
            {method: "DELETE"}
        ).then(it => it.json())
    },
}


export const DevicesApi = {

    rescan: async (): Promise<any> => {
        return await fetch(`/devices/rescan`,
            {
                method: "POST",
            }
        )
    },

    unassign: async (id: number, outputId: number): Promise<any> => {
        return await fetch(`/devices/${id}/${outputId}`,
            {
                method: "DELETE",
            }
        )
    },

    list: async (): Promise<SoundDevice[]> => {
        return await fetch(`/devices/`, {method: "GET"}).then(it => it.json())
    },

    assign: async (id: number, outputId: number): Promise<any> => {
        return await fetch(`/devices/${id}/${outputId}`,
            {
                method: "PUT",
            }
        )
    },

}