

export interface SoundFile {
    name: string,
    loop: boolean,
    volume: number
    id?: number
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

