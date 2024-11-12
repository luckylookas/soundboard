
/*
POST /files/rescan
GET /files/query?query=
GET /files/{collection}
GET /files
DELETE /files/{collection}/{name}
POST /files/{collection}
 */

export interface OutputState {
    name: string,
    label: string,
    state: string,
    currentlyPlaying?: string,
}

export interface SoundFile {
    name: string,
    collection: string,
}

export interface Scene {
    name: string,
    mappings: SceneMapping[]
    hotbar: HotBar[]
}

export interface SceneMapping {
    file: SoundFile,
    output: string,
    loop: boolean,
    volume: number
}

export interface HotBar {
    file: SoundFile,
    loop: boolean,
    volume: number
}


export const Scenes =  {

    setScene: async (scene: Scene): Promise<any> => {
        return await fetch(`/scene/${scene.name}`, {method: "PUT", body: JSON.stringify(scene)})
    },

    getScene: async (scene: Scene): Promise<Scene> => {
        const ret = await fetch(`/scene/${scene.name}`)
        return await ret.json()
    },

    play: async (scene: Scene): Promise<any> => {
        return await fetch(`/scene/${scene.name}/play`, {method: "PUT"})
    },

    stop: async (scene: Scene): Promise<any> => {
        return await fetch(`/scene/${scene.name}/stop`, {method: "PUT"})
    },

}

export const Library = {

    rescan: async (): Promise<any> => {
        const it = await fetch("/files/rescan", {
            method: "POST"
        })
        return await it.json()
    },

    getCollections: async (): Promise<string[]> => {
        const it = await fetch("/files/")
        return await it.json()
    },

    getCollection: async (name: string): Promise<SoundFile[]> => {
        const it = await fetch(`/files/${name}`)
        return await it.json()
    },

    query: async (query: string): Promise<SoundFile[]> => {
        const it = await fetch(`/files/query?query=${query}`)
        return await it.json()
    },
    delete: async (file: SoundFile): Promise<any> => {
        const it = await fetch(`/files/${file.collection}/${file.name}`, {method: "DELETE"})
        return await it.json()
    },

    upload: async (collection: string, file: File, name?: string): Promise<SoundFile[]> => {
        const formData  = new FormData();
        formData.append('file', file, name ?? file.name);

        const it = await fetch(`/files/${collection}${name ? `?name=${name}` : ''}`, {
            method: "POST",
            body: formData,
        })
        return await it.json()
    },
}


export const Output = {

    stop: async (name?: string): Promise<any> => {
        return await fetch(`/outputs${name ? `/${name}/` : '/'}stop`, {
            method: "PUT"
        })
    },

    identify: async (name: string): Promise<any> => {
        return await fetch(`/outputs/${name}/identify`, {
            method: "POST"
        })
    },

    setVolume: async (name: string, volume: number): Promise<any> => {
        return await fetch(`/outputs/${name}/volume/${volume}`, {
            method: "POST"
        })
    },

    play: async (name: string, file: SoundFile, volume?: number, loop?: boolean): Promise<any> => {
        return await fetch(`/outputs/${name}/play${loop ? `?loop=true` : '?loop=false'}${volume ? `&volume=${volume}` : ''}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(file)
        })
    },

    list: async (): Promise<OutputState[]> => {
        const it = await fetch(`/outputs`)
        return await it.json()
    },

    relabel: async (name: string, label: string): Promise<any> => {
        return await fetch(`/outputs/${name}/label/${label}`, {
            method: "POST"
        })
    },

    reload: async (): Promise<any> => {
        return await fetch(`/outputs/reload`, {
            method: "POST"
        })
    },

}