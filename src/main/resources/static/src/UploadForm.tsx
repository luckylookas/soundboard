import React, {useState} from 'react';
import {Library} from "./api";
import {Box, Button, HtmlFileInput, Slider, Toggle} from "./components/Button";

function UploadForm() {
    const [loop, setLoop] = useState(false)
    const [volume, setVolume] = useState(100)
    const [file, setFile] = useState<File | undefined>(undefined)
    const [history, setHistory] = useState<{name: string, success: boolean}[]>([])
    return <Box title={"upload"}>
        <div className={'flex flex-col py-2 flex-grow-0'}>
            <Toggle onClick={() => setLoop(old => !old)} checked={loop}>{loop ? 'loop on' : 'loop off'}</Toggle>
            <Slider value={volume} onChange={setVolume}><span className={`text-neutral-800`}>{`volume ${volume}%`}</span></Slider>
            <HtmlFileInput onChange={setFile} id={"file"} value={file} inputProps={{accept: ".mp3"}} />
        </div>

        <div className={'w-full flex flex-grow flex-col justify-start items-start py-2'}>
            {history.map(it =>
                <div className={'flex flex-row w-full justify-between items-center'}>
                    <div className={'flex'}>{it.name}</div>
                    <div className={'flex'}>{it.success ? 'ok': 'failed'}</div>
                </div>)}
        </div>

        <div className={'flex flex-col py-2 flex-grow-0 '}>
            <Button disabled={!file} onClick={() => {
                Library.upload(file!, loop, volume)
                    .then((r) => {
                        setHistory(old => [{name: r.name, success: true} ,...old].slice(0, 10))
                        setFile(undefined)
                    })
                .catch(
                    () => setHistory(old => [{name: file!.name, success: false} ,...old].slice(0, 10))
                )}} text={'upload'}/>
        </div>
    </Box>
}

export default UploadForm;
