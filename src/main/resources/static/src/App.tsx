import React, {useEffect, useState} from 'react';
import AdventureForm, {AdventureList} from "./AdventureForm";
import {AdventureContext, SoundboardContext} from "./Context";
import {BrowserRouter, Route, Routes} from "react-router";
import {DevicesApi, FilesApi} from "./api";
import UploadForm from "./UploadForm";


function App() {
    const context = useState<SoundboardContext|undefined>()

    useEffect(() => {
        DevicesApi.list().then(it => context[1](prev => ({...prev, devices: it})))
        FilesApi.find("").then(it => context[1](prev => ({...prev, files: it})))
    }, []);

    // useEffect(() => {
    //     const to = setTimeout(() => {
    //         DevicesApi.list().then(it => context[1](prev => ({...prev, devices: it})))
    //     }, 1000)
    //     return () => clearTimeout(to)
    // }, [context]);

    return <BrowserRouter>
        <AdventureContext.Provider value={context}>
        <div id="body" className='flex justify-center items-start h-full p-5 bg-neutral-800'>
            <div className={`h-full w-full text-neutral-100`}>
                <Routes>
                    <Route path={"/upload"} element={<UploadForm />}></Route>
                    <Route index path={"/"} element={<AdventureList />} />
                    <Route path={':adventureId'} element={<AdventureForm />} />
                    <Route path={':adventureId/:sceneId'} element={<AdventureForm />} />
                </Routes>
            </div>
        </div>
        </AdventureContext.Provider>
    </BrowserRouter>
}

export default App;
