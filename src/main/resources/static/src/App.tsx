import React from 'react';
import UploadForm from "./UploadForm";


function App() {
    return <div id="body" className='flex justify-center items-start h-full p-5 bg-emerald-50'>
        <div className={`h-1/2 w-full`}>
            <UploadForm />
        </div>
    </div>
}

export default App;
